const chalk = require('chalk');

module.exports.info = chalk.blue;
module.exports.error = chalk.red;
module.exports.debug = chalk.yellow;
module.exports.log = console.log;