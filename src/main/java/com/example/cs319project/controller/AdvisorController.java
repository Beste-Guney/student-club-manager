package com.example.cs319project.controller;

import com.example.cs319project.dto.AdvisorDto;
import com.example.cs319project.dto.ClubDto;
import com.example.cs319project.model.*;
import com.example.cs319project.model.clubstrategy.ClubRole;
import com.example.cs319project.model.clubstrategy.ClubRoleName;
import com.example.cs319project.model.request.*;
import com.example.cs319project.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

//This is the controller for advisor business
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/advisor")
public class AdvisorController {

    private final ClubService clubService;
    private final ClubRoleService clubRoleService;
    private final StudentService studentService;
    private final EventService eventService;
    private final AdvisorService advisorService;
    private final NotificationService notificationService;

    @GetMapping(value= "/advisorView", produces =  MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<Advisor> advisorView(@RequestParam(name = "id") int idHolder) {
        Advisor advisor = advisorService.findById(idHolder);
        return ResponseEntity.ok(advisor);
    }

    @GetMapping(value= "/advisorOfClubView", produces =  MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<Advisor> advisorOfClubView(@RequestParam(name = "id") int  clubId) {
        Club club = clubService.findById(clubId);
        Advisor advisor = advisorService.findByClub(club);
        return ResponseEntity.ok(advisor);
    }

    @GetMapping(value = "/allAdvisors", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<List<Advisor>> allAdvisors(){
        return ResponseEntity.ok(advisorService.findAll());
    }

    //if advisor rejects an event members should be notified
    @PostMapping(value = "/rejectEvent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> rejectEvent(@Valid @RequestBody IdHolder idHolder) {
        Event event = eventService.findByEventId(idHolder.getId());
        if(event == null) {
            return ResponseEntity.ok(new MessageResponse("There is no such an event"));
        }
        event.setStatus("REJECTED");
        eventService.saveEvent(event);

        Set<Club> notifieds = new HashSet<>();
        notifieds.add(clubService.findById(event.getClubId()));

        String str = "Event with name " + event.getName() + " is rejected!";

        Notification notification = Notification.builder()
                .date(null)
                .description(str)
                .clubId(event.getClubId())
                .name(event.getName())
                .isRequest(false)
                .notified_clubs(notifieds).build();
        notificationService.createNewNotification(notification);

        return ResponseEntity.ok(new MessageResponse("Event has been rejected successfully"));
    }

    // if an event is accepted, users should be notified and event status should be updated
    @PostMapping(value = "/acceptEvent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> acceptEvent(@Valid @RequestBody IdHolder idHolder) {
        Event event = eventService.findByEventId(idHolder.getId());
        if(event == null) {
            return ResponseEntity.ok(new MessageResponse("There is no such an event"));
        }
        event.setStatus("ACCEPTED");
        eventService.saveEvent(event);

        Set<Club> notifieds = new HashSet<>();
        notifieds.add(clubService.findById(event.getClubId()));


        String str = "Event with name " + event.getName() + " is accepted!";

        Notification notification = Notification.builder()
                .date(null)
                .description(str)
                .clubId(event.getClubId())
                .name(event.getName())
                .isRequest(false)
                .notified_clubs(notifieds).build();
        notificationService.createNewNotification(notification);

        List<ClubRole> clubRoles = clubRoleService.findByClubId(event.getClubId());
        Set<Student> notifieds_p = new HashSet<>();

        for(ClubRole role: clubRoles){
            Student student = studentService.findById(role.getStudent().getId());
            notifieds_p.add(student);
        }

        String str2 = "There is a new Event with name " + event.getName();

        Notification notification_p = Notification.builder()
                .date(event.getEventDate())
                .description(str2)
                .clubId(event.getClubId())
                .isRequest(false)
                .name(clubService.findById(event.getClubId()).getName())
                .notified_people(notifieds_p).build();
        notificationService.createNewNotification(notification_p);

        return ResponseEntity.ok(new MessageResponse("Event has been accepted successfully"));
    }

    //advisors can be assigned to new clubs
    @PostMapping(value = "/advisorUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> assignAdvisor(@Valid @RequestBody JoinClubRequest request){
        Club club = clubService.findById(request.getClubId());
        if(club == null){
            return ResponseEntity.ok(new MessageResponse("There is no such a club"));
        }

        Advisor advisor = advisorService.findById(request.getStudentId());
        if(advisor == null){
            return ResponseEntity.ok(new MessageResponse("There is no such an advisor"));
        }
        advisor.setClub(club);
        advisorService.createNewAdvisor(advisor);
        return ResponseEntity.ok(new MessageResponse("Advisor updated"));
    }

    @GetMapping(value = "/findAdvisorClub", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Club> returnClubOfAdvisor(@RequestParam(name="id") int id){
        Advisor advisor = advisorService.findById(id);
        if(advisor == null){
           return ResponseEntity.ok(null);
        }

        Club club = advisor.getClub();
        if(club == null){
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.ok(club);
    }

}
