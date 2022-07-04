const handler = require('serve-handler');
const { fn_create_api_spec_inst } = require('../common/spec');
const common_utils = require('../common/utils');
const http = require('http');
const { promisify } = require('util');
const { parse } = require('url');
const compression = require('compression');
const compressionHandler = promisify(compression());

common_utils.log.debug('received config file');

let config = {
	// public: '/Users/jerrylai/Documents/PersonalProjects/denote-fe/public_repository/server/node/common',
	public: '/Users/jerrylai/Documents/PersonalProjects/denote-fe/public_repository/DSL',
	cleanUrls: true, // if true, then will trace .html affix
	rewrites: [],
	redirects: [],
	headers: [],
	directoryListing: true,
	unlisted: [],
	trailingSlash: true, // if folder is a folder, then will append / as its affix value
	renderSingle: false, // if there's only one single file in the folder, then will return its value directly
	symlinks: true,
	compress: true,
	etag: true,
};

let compress = config.compress;

const server = http.createServer(async (request, response) => {
	if (compress) {
		await compressionHandler(request, response);
	}
	return handler(request, response, config);
});

let port = 3000;

server.listen(port, () => {
	common_utils.log.info('Running at http://localhost:' + port);
});

setInterval(() => {
	common_utils.log.info('Keep running' + new Date().getTime());
	// common_utils.log.warn('Keep running' + new Date().getTime());
	// common_utils.log.error('Keep running' + new Date().getTime());
}, 4000);

module.exports = fn_create_api_spec_inst({
	api: {},
});
