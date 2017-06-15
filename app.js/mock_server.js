const express = require('express');
const data = require('./data').data;

let app = express();
let mockData = (req, res, next) => {
  res.status(200)
    .send(data);
};

app.get('/', mockData);
app.listen(9999, () => {
  console.log("mock data server on port 9999");
});
