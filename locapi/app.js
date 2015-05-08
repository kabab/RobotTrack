var express = require('express');
var app = express();
var bodyParser = require('body-parser'); //bodyparser + json + urlencoder
var morgan  = require('morgan'); // logger
var config = require('./config');
var mustacheExpress = require('mustache-express');
var path = require('path');

app.engine('html', mustacheExpress());
app.set('view engine', 'html');
app.set('views', __dirname + '/views');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded());
app.use(morgan('dev'));
app.use(express.static(path.join(__dirname, 'public')));

app.listen(config.api_port);


// For testing
var data = {
  "1": -100,
  "2": -80
};

// For production
/*
var data = {
};
*/



app.use( function(req, res, next) {
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Credentials', true);
  res.set('Access-Control-Allow-Methods', 'GET, POST, DELETE, PUT');
  res.set('Access-Control-Allow-Headers', 'X-Requested-With, Content-Type, Authorization');
  if ('OPTIONS' == req.method) return res.send(200);
  next();
});

app.post('/data', function(req, res) {
  data[req.body.device_id] = req.body.rssi;
  res.send(200);
});

app.get('/data', function(req, res) {
  res.json(data);
});
app.get('/map', function(req, res) {
  return res.render('index');
});

console.log('API start on %d', config.api_port);
