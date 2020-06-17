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
import com.google.gson.Gson;
import com.google.sps.servlets.Comment;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

/** Servlet that deletes comment data*/
@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

  @Override

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query commentQuery = new Query("Comment");
      PreparedQuery storedComments = datastore.prepare(commentQuery);
      
      for(Entity comment : storedComments.asIterable()){
          datastore.delete(comment.getKey());
      }

      Query commentDataQuery = new Query("CommentData");
      PreparedQuery storedCommentData = datastore.prepare(commentDataQuery);

      for(Entity commentData : storedCommentData.asIterable()){
          datastore.delete(commentData.getKey());
      }

      response.sendRedirect("/index.html#comment-section");

  }
}
