// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(displayCommentData);

/** Creates a chart and adds it to the page. */
function displayCommentData() {
  fetch('/comment-data').then(response => response.json()).then((commentData) => {
      if(commentData == null){
        document.getElementById('chart-container').innerText = "There are currently no comments"; 
      } else {
          drawChart(commentData);
      }
  });
}

function drawChart(commentData) { 
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Sentiment');
  data.addColumn('number', 'Count');
  data.addRows([
      ['Positive', commentData.propertyMap.positive],
      ['Negative', commentData.propertyMap.negative],
  ]);
  const options = {
      'title': 'Comment Sentiment Data',
      'width':500,
      'height':400
  };

  const chart = new google.visualization.PieChart(document.getElementById('chart-container'));
  chart.draw(data, options);
}



