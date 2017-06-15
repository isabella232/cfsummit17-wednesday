//region require
const express = require('express');
const request = require('superagent');
const HazelcastClient = require('hazelcast-client').Client;
const Config = require('hazelcast-client').Config;
const listener = require('./listener');
const info = require('./out').info;
const error = require('./out').error;
const debug = require('./out').debug;
const log = require('./out').log;
const Promise = require('bluebird');
//endregion

//region for pcfdev
// environment variable VCAP_SERVICES comes as a string
//PCFDev
let myvcap = "{\n" +
  "    \"hazelcast\": [{\n" +
  "        \"credentials\": {\"host\": \"127.0.0.1\"},\n" +
  "        \"syslog_drain_url\": null,\n" +
  "        \"volume_mounts\": [],\n" +
  "        \"label\": \"hazelcast\",\n" +
  "        \"provider\": null,\n" +
  "        \"plan\": \"plan1\",\n" +
  "        \"name\": \"cfsummit\",\n" +
  "        \"tags\": [\"hazelcast\"]\n" +
  "    }]\n" +
  "}";
//PCF
/*let myvcap = "{\"hazelcast\":[{ \"credentials\": { \"group_name\": \"dev\", \"group_pass\": \"dev-pass\", \"members\": [ \"10.0.32.13\", \"10.0.32.11\", \"10.0.32.12\" ] }, \"syslog_drain_url\": null, \"volume_mounts\": [ ], \"label\": \"hazelcast\", \"provider\": null, \"plan\": \"t2.micro\", \"name\": \"cfsummit\", \"tags\": [ \"hazelcast\" ] }]}";*/
//endregion

//region read VCAP_SERVICES from environment
const vcap = process.env.VCAP_SERVICES || myvcap;
log(`${info.bold('VCAP_SERVICES')}: ${debug(JSON.stringify(vcap, null, 2))}`);
let members = [];
const host = JSON.parse(vcap).hazelcast[0].credentials.host;
if (!!host) {
  log(info.bold(`Found ${host} in VCAP_SERVICES`));
  members.push({host: host, port: '5701'});
}
else {
  let members2 = JSON.parse(vcap).hazelcast[0].credentials.members;
  members2.forEach((m) => {
    members.push({host: m, port: '5701'});
  })
}
//endregion

const PORT = process.env.PORT || 3000;
const app = express();

let prepareResponse = (org, numberOfRepos) => {
  return {response: `Organization "${org}" has ${numberOfRepos} public repositories`};
};

//region middleware handlers
let index = (req, res, next) => {
  res.render('index', {
    title: "Hazelcast Node.js client", message: 'Some links', links: [
      {desc: 'prints VCAP_SERVICES', link: '/vcap'},
      {desc: 'fetches remote orgs and displays stats', link: '/stats/:org'},
      {desc: 'bypass cached response', link: '/stats/:org/bypass'},
      {desc: 'gh repo info mirror (multimap example)', link: '/orgs/:org/repos'}
    ]
  })
};

let printVcap = (req, res, next) => {
  res.send(vcap);
};

let cache = (req, res, next) => {
  const org = req.params.org;

  console.time('cache hit');
  orgCache.get(org).then((result) => {
    if (result !== null) {
      console.timeEnd('cache hit');
      res.send(prepareResponse(org, result));
    } else {
      next();
    }
  });
};

let getNumberOfRepos = (req, res, next) => {
  // local mock server
  //const requestUrl = 'http://localhost:9999';

  const organization = req.params.org;
  const requestUrl = `https://api.github.com/orgs/${organization}/repos?per_page=100`;

  request.get(requestUrl, (err, response) => {
      if (err) {
        throw err;
      }
      let body = response.body;
      let repoNumber = 0;
      let promiseResults = [];

      if (response && body) {
        repoNumber = body.length;

        promiseResults.push(orgCache.putIfAbsent(organization, repoNumber));
        body.forEach((repo) => {
          promiseResults.push(orgMultiMap.put(organization, repo));
        });

        // wait until records will be written to map and multimap
        Promise.all(promiseResults)
          .then(() => res.send(prepareResponse(organization, repoNumber)))
      }
    }
  );
};

let localRepos = (req, res) => {
  let organization = req.params.org;
  orgMultiMap.get(organization).then((val) => {
    res.send(val);
  })
};
//endregion

let initConfig = (members, nearCache) => {
  let config = new Config.ClientConfig();
  config.networkConfig.addresses = members;

  //region NearCache
  if (!!nearCache) {
    let orgsNearCacheConfig = new Config.NearCacheConfig();
    orgsNearCacheConfig.invalidateOnChange = true;
    orgsNearCacheConfig.name = 'orgsMap';

    let ncConfigs = {};
    ncConfigs[orgsNearCacheConfig.name] = orgsNearCacheConfig;
    config.nearCacheConfigs = ncConfigs;
  }
  log(debug(JSON.stringify(config, null, 2)));
  return config;
  //endregion
};

let orgCache;
let orgMultiMap;

HazelcastClient.newHazelcastClient(initConfig(members, true)).then((client) => {
  const name = 'orgs';

  orgCache = client.getMap(`${name}Map`);
  orgCache.addEntryListener(listener, undefined, true);
  orgMultiMap = client.getMultiMap(`${name}MultiMap`);

  app.set('view engine', 'pug');
  app.get('/', index);
  app.get('/vcap', printVcap);
  app.get('/stats/:org', cache, getNumberOfRepos);
  app.get('/stats/:org/bypass', getNumberOfRepos);
  app.get('/orgs/:org/repos', localRepos);

  app.listen(PORT, () => {
    log(`${info('Express Web App listening on port')} : ${info.bold(PORT)}`);
  });
});
