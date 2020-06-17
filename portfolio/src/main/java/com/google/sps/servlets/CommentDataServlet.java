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
@WebServlet("/comment-data")
public class CommentDataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    Query query = new Query("CommentData");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery data = datastore.prepare(query);
    Entity commentData = handleRetrievedData(data);

    String json = convertToJsonUsingGson(commentData);
    response.getWriter().println(json);
  }

  
  private String convertToJsonUsingGson(Object data) {
    Gson gson = new Gson();
    String json = gson.toJson(data);
    return json;
  }

  /** It is possible that doGet will be validly called but somehow:
   * 1. There does not exist any commentData entity OR
   * 2. There are more than one commentData entities 
   * This method handles either occurence. Both are returned as null, which will cause no data to be returned. 
   */
  private Entity handleRetrievedData(PreparedQuery data) {
    Entity commentData = null;
    try {
      commentData = data.asSingleEntity();
    }
    catch(TooManyResultsException e) {
      return null;
    }
    finally {
      return commentData; 
    }
  }
}