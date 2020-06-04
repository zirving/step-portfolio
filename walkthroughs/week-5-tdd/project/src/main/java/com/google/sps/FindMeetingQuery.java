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

private Collection<TimeRange> validTimes; 
private Collection<TimeRange> invalidTimes; 

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    HashSet<Event> meetings = events;
    HashSet<String> attendees = request.getAttendees();

    if(attendees.isEmpty()) {
        TimeRange wholeDay = TimeRange.WHOLE_DAY;
        HashSet<TimeRange> noAttendees = new HashSet<TimeRange>();
        noAttendees.add(wholeDay);
        return noAttendees; 
    }
    if(request.getDuration() > TimeRange.WHOLE_DAY.duration()){
        return new HashSet<TimeRange>();
    }

    

    for(Event meeting : meetings){
        if(containsSharedAttendees(meeting.getAttendees(), attendees)) {
            invalidTimes.addTime(meeting.getWhen());
        }
    }
  }
  
  /** @return true if the two sets contain any shared attendees. 
   * Uses retainAll() to gather the intersection of the sets, and then checks if it is empty
   */
  public boolean containsSharedAttendees(Collection<String> attendeeSet1, Collection<String> attendeeSet2) {
    attendeeSet1.retainAll(attendeeSet2);
    if(attendeeSet1.isEmpty()){
      return false;
    } else {
      return true;
    }
  }

  /** 


}