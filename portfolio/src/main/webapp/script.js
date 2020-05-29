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

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

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

/**
 * Fetches data from the data servlet
 */
function getDataFromServlet(){
 fetch('/data').then(response => response.json()).then((data) => { 
     const dataElement = document.getElementById("servlet-msg-container");
     dataElement.innerHTML = '';
     dataElement.appendChild(document.createTextNode(" " + data[0]));
     dataElement.appendChild(document.createTextNode(" " + data[1]));
     dataElement.appendChild(document.createTextNode(" " + data[2]));
 });
}