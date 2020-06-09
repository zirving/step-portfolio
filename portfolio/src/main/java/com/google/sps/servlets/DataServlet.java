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

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  public static final String COMMENT_DATA_KEY = "capitalizationData"; 
  public static final String UPPERCASE_PROPERTY = "upperCase";
  public static final String LOWERCASE_PROPERTY = "lowerCase";

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

      //updateCommentData(comment);

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
   * TODO: Updates the sentiment analysis data for the comment section (currently uses a placeholder)
   */
  private void updateCommentData(String comment) {
    Query query = new Query("CommentData");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery data = datastore.prepare(query);

    Entity commentData = handleRetrievedData(data);

    int[] capitalizationData = analyzeComment(comment); 
    int numLower = (int)commentData.getProperty(LOWERCASE_PROPERTY) + capitalizationData[0];
    int numUpper = (int)commentData.getProperty(UPPERCASE_PROPERTY) + capitalizationData[1];

    createCommentDataEntity(numLower, numUpper);
  }

  /**
  * @return TODO: an array containing the sentiment data to be stored (currently uses a placeholder)
  */
  private int[] analyzeComment(String comment) {
    if(Character.isUpperCase(comment.charAt(0))) {
      return new int[] {0,1};
    } else {
      return new int[] {1,0};
    }
  }
  
  /** It is possible that updateCommentData will be validly called but somehow:
   * 1. There does not exist any commentData entity OR
   * 2. There are more than one commentData entities 
   * This method handles either occurence. 
   */
  private Entity handleRetrievedData(PreparedQuery data) {
    Entity commentData = null; 
    try {
      commentData = data.asSingleEntity();
    }
    catch(TooManyResultsException e) {
      data = handleExtraResults(data);
      commentData = data.asSingleEntity();
    }
    finally {
      return commentData; 
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

  private void createCommentDataEntity(int numLower, int numUpper) {
    Entity commentDataEntity = new Entity("CommentData", COMMENT_DATA_KEY);
    commentDataEntity.setProperty(LOWERCASE_PROPERTY, numLower);
    commentDataEntity.setProperty(UPPERCASE_PROPERTY, numUpper);
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentDataEntity);
  }
}

