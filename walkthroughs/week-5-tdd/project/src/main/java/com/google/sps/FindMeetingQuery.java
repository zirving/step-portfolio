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

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    Collection<Event> meetings = events;
    Collection<String> attendees = request.getAttendees();
    ArrayList<TimeRange> validTimes = new ArrayList<TimeRange>();
    validTimes.add(TimeRange.WHOLE_DAY);

    if(request.getDuration() > TimeRange.WHOLE_DAY.duration()){
        return new ArrayList<TimeRange>();
    }
    
   
    for(Event meeting : meetings){
        if(containsSharedAttendees(meeting.getAttendees(), attendees)) {
          validTimes = removeInvalidTime(validTimes, meeting.getWhen());
        }
    }
    validTimes.removeIf(i -> i.duration() < request.getDuration());

    
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    ArrayList<TimeRange> validTimesWithOptional= validTimes;
    for(Event meeting : meetings){
        if(containsSharedAttendees(meeting.getAttendees(), optionalAttendees)) {
          validTimesWithOptional = removeInvalidTime(validTimesWithOptional, meeting.getWhen());
        }
    }
    validTimesWithOptional.removeIf(i -> i.duration() < request.getDuration());

    if(validTimesWithOptional.isEmpty()) {
      return validTimes;
    } else {
      return validTimesWithOptional;
    }
  }
  
  /** @return true if the two sets contain any shared attendees. 
   * Uses retainAll() to gather the intersection of the sets, and then checks if it is empty
   */
  public boolean containsSharedAttendees(Collection<String> attendeeSet1, Collection<String> attendeeSet2) {
   
    List<String> attendeeSet1Copy = new ArrayList<String>(attendeeSet1);
    attendeeSet1Copy.retainAll(attendeeSet2);
    if(attendeeSet1Copy.isEmpty()){
      return false;
    } else {
      return true;
    }
  }
  
  /**
   * @return The new list of valid time ranges with the invalid range removed 
   */
  public ArrayList<TimeRange> removeInvalidTime(Collection<TimeRange> validTimes, TimeRange invalidTime){ 
    
    ArrayList<TimeRange> newValidTimes = new ArrayList<TimeRange>();

    for(TimeRange validTime : validTimes) {
      if(validTime.end() < invalidTime.start()){
          newValidTimes.add(validTime);
      }
      if(validTime.contains(invalidTime.start())) {
        TimeRange splitRange = TimeRange.fromStartEnd(validTime.start(), invalidTime.start(), false);
        newValidTimes.add(splitRange);
      }
      if(validTime.contains(invalidTime.end())) {
        TimeRange splitRange = TimeRange.fromStartEnd(invalidTime.end(), validTime.end(), false);
        newValidTimes.add(splitRange);
      }
      if(validTime.start() > invalidTime.end()) {
        newValidTimes.add(validTime);
      }
    }
    return newValidTimes;    
  }
}