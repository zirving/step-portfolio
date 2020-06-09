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
google.charts.setOnLoadCallback(drawChart);


var DEFAULT_COMMENT_LIMIT = '10'; 

/**
 * Adds a random quote to the page.
 */
function addRandomQuote() {
  const quotes =
      ['"Hakuna Matata" - Timon and Pumbaa', '"The Dude abides." - The Dude', 
      '"Strange women lying in ponds distributing swords is no basis for a system of government." - Dennis',
      "\"I'm sorry Dave, I'm afraid I can't do that.\" - HAL 9000", '"Very nice." - Borat'];

  // Pick a random quote.
  const chosenQuote = quotes[Math.floor(Math.random() * quotes.length)];

  // Add it to the page.
  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = chosenQuote;
}

/**
 * Loads a music video from a clicked band. 
 */
function loadMusicVideo(){
  const videoContainer = document.getElementById('video-container');
  videoContainer.empty(); 
  videoContainer.append("https://www.youtube.com/watch?v=-wVWjl9Kq6U");

}

function displayComments(commentLimit){
  if(typeof commentLimit === 'undefined'){
      commentLimit = DEFAULT_COMMENT_LIMIT;
  }
  const url = '/data?comment-limit=' + commentLimit;
  fetch(url).then(response => response.json()).then((commentHistory) => {
    const commentSection = document.getElementById("existing-comments");
    if(commentHistory.length == 0) {
      commentSection.innerHTML = " <p> No comments yet. Leave yours!</p>";
      return;
    } else {
      document.getElementById("existing-comments").innerHTML = "";
      for(i = 0; i<commentHistory.length;i++){
        createCommentElement(commentHistory[i].username, commentHistory[i].content);
      }
    }
  });
}

function updateCommentLimit(){
  const commentLimit = document.getElementById("comment-limit-selector").value; 
  displayComments(commentLimit);
}

function deleteComments(){
    const method = {method : 'POST'};
    fetch('/delete-data', method).then(displayComments());
}

function createCommentElement(user, comment){
  const commentElement = document.createElement('div');

  const username = document.createElement('h3');
  username.appendChild(document.createTextNode(user));

  const content = document.createElement('p');
  content.appendChild(document.createTextNode(comment));

  const divider = document.createElement('hr');

  commentElement.appendChild(username);
  commentElement.appendChild(content);
  commentElement.appendChild(divider);

  document.getElementById("existing-comments").appendChild(commentElement);
}

/** Creates a chart and adds it to the page. */
function drawChart() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Animal');
  data.addColumn('number', 'Count');
        data.addRows([
          ['Lions', 10],
          ['Tigers', 5],
          ['Bears', 15]
        ]);

  const options = {
    'title': 'Zoo Animals',
    'width':500,
    'height':400
  };

  const chart = new google.visualization.PieChart(
      document.getElementById('chart-container'));
  chart.draw(data, options);
}


