# Wednesday

[WednesdayAddams]: src/site/markdown/images/wednesday-addams.jpeg "Image wednesday-addams.jpeg"

A demo of the Hazelcast PCF Tile, written on a Wednesday

![Amusing image][WednesdayAddams] 

Do not share, the UI is laughable and there's a license key in the JSON files.

# To use on a full PCF environment, with the Tile ready uploaded

1 Login

`cf login -a api.system.pcf.hazelcast.com`

Refer to the wiki for credentials. I use `admin` login which is probably excessive.

* Validate

Try `cf marketplace`, there should be a `hazelcast` entry for the Tile. Against this tile should be some pre-defined service plans, where a plan defines the virtual machine strength.

2 Create a **Service**

`cf create-service hazelcast t2.micro cfsummit -c ..../before.json`

A **Service** in CloudFoundry terms is a back-end application, available for front-end applications to *bind* to.

The back-end application here that the Tile holds is a Hazelcast IMDG server. Specifically, `hazelcast-enterprise.jar` and nothing else. No user code, no other Hazelcast jars.

What the *create-service* command is doing here is selecting the *hazelcast* Tile, the *t2.micro* service plan (a small machine, something like 2 CPUs, 4Gb RAM) to form a service named *cfsummit*.

The file *before.json* is used to configure the service. Two important
ones are `instanceCount` which dictates how many virtual machines to make, and
`mancenterAppName`.

* Validate

Try `cf s`, the service should take a few minutes to build as it is spinning up AWS instances.

3 Mancenter

Once the service is started, the Mancenter will be available at `http://mancenter-cfsummit.apps.pcf.hazelcast.com/mancenter`.

Note the URL is the concatenation of `mancenterAppName` from the JSON configuration file, and the base domain for the PCF environment.

4 On a demo

What this means is three AWS instances are built for Hazelcast servers and one for the Mancenter.

Mancenter doesn't seem to be the last one to be built, though I don't know if you can rely on that. But it's worth trying that URL before the service creation has completed.

5 Application time

Deploy the application (Hazelcast Client).

On STS, using the Boot Dashboard panel. Probably a similar idea on Intellij.

This uses _manifest.yml_.

In this file, *route: app.apps.pcf.hazelcast.com*  names the URL that the application will be available on. And *services: cfsummit* names the service(s) that the front-end application is bound to.

If successful, the application will be available as `http://app.apps.pcf.hazelcast.com/`

6 Scaling time

Now try

`cf update-service cfsummit -c ..../after.json`

This applies the new configuration file _after.json_ to the running service.

What should happen is one at a time the servers are removed and restored. Not the Mancenter though, so you can keep this open and watch.

CF waits for the _isClusterSafe()_ signal from a new VM before proceeding to the next one, and it's building AWS instances, so the whole thing will take a few minutes. So, get everyone in the room to use the application while this occurs.

Comparing _before.json_ and _after.json_ you'll see it's just the count of the number of servers that changes.

PCF (v1.10) isn't yet bright enough to spot that the optimisation available to just start an extra server. As far as it's is concerned the
configuration has changed so each process needs restarted to pick up
the new configuration.

# To **hack** use on a PCF Dev environment, no Tile applicable

This is a _hack_. The Hazelcast server is external to the CloudFoundry
environment. The application with the Hazelcast client is internal to
the CloudFoundry environment. We create a dummy service with no implementation, so the application can bind to it and retrieve
environment variables for the external application's address.

Really, we may as well provide that external address in the application's
properties.

1 Create a **Service**

Use _cups_ (Create User Provided Service) with the _svc_ jar file in the project.

`cf cups cfsummit -p "host,port"`

When prompted, enter the external IP address of your localhost, then _5701_. (eg. `ifconfig | grep inet`).

This makes a dummy service in PCF Dev, basically a way of passing binding
parameters to the application.

* Validate

`cf s`

2 Start the service

From your localhost, `java -jar svc/target/svc.jar`

3 Deploy the application to PCF Dev

As before, varies if you do from IDE.

From command line

`cf bind-service app cfsummit`

`cf restage cfsummit`

*NOTE* Make sure the _manifest.yml_ specifies the correct route.
For PCF Dev it will be "app.local.pcfdev.io"

* Validate

`cf a`

and

`cf env app`

Then try `http://app.local.pcfdev.io`

# To **properly** use on a PCF Dev environment, no Tile applicable

We need a _service broker_.

1 Deploy **svc** as an _application_

```
cf push -f svc/manifest-pcfdev.yml -p svc/target/svc.jar
```

Look in the start-up logs for the password generated. On the console, or ` cf logs svc --recent`.

```
2017-06-13 13:24:27.809  INFO 7 --- [           main] b.a.s.AuthenticationManagerConfiguration : 
Using default security password: 19c17490-25c9-44ec-a4f2-60cd6c69a710
```

So the password is `19c17490-25c9-44ec-a4f2-60cd6c69a710`, and this is for an account `user`.

* Validate

When this is done, `cf a` should list it.

2. Turn that application into a service broker

A service broker creates instances.

The command is

```
cf create-service-broker hazelcast-service-broker user 9b71a499-9665-453f-aff2-1cbdad373ca1 http://svc.local.pcfdev.io --space-scoped
```

With the password from the previous step.

* Validate

Try `cf m` to see if it is listed on the marketplace.

3 Create the service

Now try

`cf create-service hazelcast plan1 cfsummit`

to create a service using the _hazelcast_ service, with the _plan1_ maching plan, and
to call that service _neil_.

4 Deploy **app** as an _application_

Make sure the _manifest.yml_ binds the app "_app_" to the service "_neil_".

```
cf push -f app/manifest-pcfdev.yml -p app/target/app.jar
```

5 Deploy **app.js** as an _application_

Make sure the _manifest.yml_ binds the app "_app.js_" to the service "_cfsummit_".

```
cf push -f app.js/manifest.yml
```

# To use without CF at all

Start the standalone service, `java -Dserver.port=0 -jar svc/target/svc.jar`

Start the application, `java -jar app/target/app.jar`

Start the Node.js client application,   `node app.js/app.js` 

Run `http://localhost:8080`

If the application gets nothing back from the Cloud Foundry lookup,
it assumes localhost and 127.0.0.1.

Watch that the service has to be a web-app so we can deploy internally on PCF Dev, so make sure it
doesn't pick 8080 or that will clash with the port the client tries to use.

# Binding

When an application is *bound* to a service, this makes information available in an environment variable `VCAP_SERVICES`.

The "_ui_" on the application allows you to view this, and you can
figure out how the client connects once you see this.

Note this is an environment variable, it doesn't change when we run
`update-service`. 

Use the `Server IPs` page on the application to see what was originally passed
and what is currently in use. Do this during or after the `update-service`.

# Application

The rest of the application shows two things, with a _Stats_ and _Person_ pages.

_AtomicLong_ to count the number of hits to any page and the front page.
Ideally you need to delete and redeploy the application while the service stays running to show distributed atomic counters.

_Person_ and _PersonRepository_ use Spring Data Hazelcast, so make
everything Spring friendly. That means `java.io.Serializable` so no good for
_Node.js_.


