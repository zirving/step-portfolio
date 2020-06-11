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
package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import com.google.gson.Gson;
import com.google.sps.servlets.Comment;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  public static final String COMMENT_DATA_KEY = "sentimentData"; 
  public static final String POSITIVE_PROPERTY = "positive";
  public static final String NEGATIVE_PROPERTY = "negative";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery storedComments = datastore.prepare(query);

    int commentLimit = getCommentLimit(request, storedComments.countEntities());
    ArrayList<Comment> commentHistory = getCommentHistory(storedComments, commentLimit);
    String json = convertToJsonUsingGson(commentHistory);
    response.getWriter().println(json);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String username = getParameter(request, "username-input", "Anonymous");
      String comment = getParameter(request, "comment-input", "");
      long timestamp = System.currentTimeMillis();

      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("username", username);
      commentEntity.setProperty("comment", comment);
      commentEntity.setProperty("timestamp", timestamp);

      updateCommentData(comment);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
  
      response.sendRedirect("/index.html#comment-section");

  }
  
  private String convertToJsonUsingGson(Object data) {
    Gson gson = new Gson();
    String json = gson.toJson(data);
    return json;
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /**
   * @return the requested number of comments to be shown, or, if none is requested, 
   * the total number of comments in the comment history. 
   */
  private int getCommentLimit(HttpServletRequest request, int total) {
    String requestedCommentLimit = getParameter(request, "comment-limit", "10");
    int commentLimit;
    if(requestedCommentLimit.equals("All")){
      commentLimit = total;
    } else {
      commentLimit = Integer.parseInt(requestedCommentLimit);
    }
    return commentLimit; 
  }

  /**
   * @return An ArrayList containing the commentHistory up to the given commentLimit
   */
  private ArrayList<Comment> getCommentHistory(PreparedQuery storedComments, int commentLimit) {
    ArrayList<Comment> commentHistory = new ArrayList<Comment>(); 
    
    Iterator<Entity> commentIterator = storedComments.asIterator();
    while((commentLimit > 0) && (commentIterator.hasNext())) {
        Entity comment = commentIterator.next();
        commentHistory.add(new Comment(
          (String) comment.getProperty("username"),
          (String) comment.getProperty("comment")));
        commentLimit--; 
    }

    return commentHistory;
  }

  /**
   * @return the Comment Data from Datastore
   */ 
  private Entity getCommentData() {
      handleCommentData(); 
      Query query = new Query("CommentData");
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery data = datastore.prepare(query);

      return data.asSingleEntity();
  }

  /**
   * TODO: Updates the sentiment analysis data for the comment section (currently uses a placeholder)
   */
  private void updateCommentData(String comment) {
    Entity commentData = getCommentData();
    long[] sentimentData;
    try{
        sentimentData = analyzeComment(comment); 
    }
    catch (IOException e){
      return;
    }
    long numNeg = (long)commentData.getProperty(NEGATIVE_PROPERTY) + sentimentData[0];
    long numPos = (long)commentData.getProperty(POSITIVE_PROPERTY) + sentimentData[1];

    createCommentDataEntity(numNeg, numPos);
  }

  /**
  * @return TODO: an array containing the sentiment data to be stored (currently uses a placeholder)
  */
  private long[] analyzeComment(String comment) throws IOException {
    Document commentAsDoc = Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(commentAsDoc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();

    if(score >= 0) {
      return new long[] {0,1};
    } else {
      return new long[] {1,0};
    }
  }
  
  /** getCommentData could fail if: 
   * 1. There does not exist any commentData entity OR
   * 2. There are more than one commentData entities 
   * This method handles either occurence. 
   */
  private void handleCommentData() {
    Entity commentData = null; 
    Query query = new Query("CommentData");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery data = datastore.prepare(query);
    try {
      commentData = data.asSingleEntity();
    }
    catch(TooManyResultsException e) {
      data = handleExtraResults(data);
      commentData = data.asSingleEntity();
    }
    finally {
      if(commentData == null){
          createCommentDataEntity(0, 0);
      }
    }
  }

 /** Removes any results that are found that are not the correct commentData entity, 
  * as determined by the correctDataKey 
  */
  private PreparedQuery handleExtraResults(PreparedQuery data) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    for(Entity dataPoint : data.asIterable()){
      if(dataPoint.getKey().getName() != COMMENT_DATA_KEY){
        datastore.delete(dataPoint.getKey());
        }
    }
    Query query = new Query("CommentData");
    PreparedQuery newData = datastore.prepare(query);
    return newData;
  }

  private void createCommentDataEntity(long numNeg, long numPos) {
    Entity commentDataEntity = new Entity("CommentData", COMMENT_DATA_KEY);
    commentDataEntity.setProperty(NEGATIVE_PROPERTY, numNeg);
    commentDataEntity.setProperty(POSITIVE_PROPERTY, numPos);
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentDataEntity);
  }
}

