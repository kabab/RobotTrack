var config = {
  'test' : {
    api_port: 3000,
  },
};

module.exports = config[process.env.NODE_ENV || 'test'];
