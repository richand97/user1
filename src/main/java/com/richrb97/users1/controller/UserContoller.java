package com.richrb97.users1.controller;

import com.richrb97.users1.document.User;
import com.richrb97.users1.service.UserService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserContoller {
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Mono<User>> addUser(@RequestBody User user){
        Mono<User> userMono = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMono);
    }
    @GetMapping
    public ResponseEntity<Flux<User>> getUser(){
        Flux<User> userFlux = userService.getUsers();
        return ResponseEntity.ok().body(userFlux);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<User>> getUserById(@PathVariable String id){
        Mono<User> userMono = userService.findUserById(id);
        return ResponseEntity.ok().body(userMono);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mono<User>> updateUser(@PathVariable String id, @RequestBody User user) {
        Mono<User> updatedUserMono = userService.updateUserById(id, user);
        return ResponseEntity.ok().body(updatedUserMono);
    }

    @GetMapping("/usersByAgeRange")
    public ResponseEntity<Flux<User>> usersByAgeRange(@RequestParam("minAge") int minAge, @RequestParam("maxAge") int maxAge){
        Flux<User> userFlux = userService.usersByAgeRange(minAge, maxAge);
        return ResponseEntity.ok().body(userFlux);
    }

    @GetMapping("/usersByTypeAddress")
    public ResponseEntity<Flux<User>> usersByTypeAddress(@RequestParam("typeAddress") String typeAddress){
        Flux<User> userFlux = userService.usersByTypeAddress(typeAddress);
        return ResponseEntity.ok().body(userFlux);
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<ByteArrayResource>> exportUsersToExcel() {
        return userService.exportUsersToExcel();
    }

    @GetMapping("/users/report/by-role")
    public Mono<Map<String, List<User>>> getUsersByRoleReport() {
        return userService.getUsers()
                .collectList()
                .map(users -> {
                    Map<String, List<User>> report = new HashMap<>();
                    for (User user : users) {
                        String role = user.getRol();
                        if (!report.containsKey(role)) {
                            report.put(role, new ArrayList<>());
                        }
                        report.get(role).add(user);
                    }
                    return report;
                });
    }

    @GetMapping("/mapGetUserByAge")
    public Mono<ResponseEntity<Map<String, List<User>>>> getUserByAge() {
        Map<String, List<User>> report = new HashMap<>();
        ArrayList<User> less18 = new ArrayList<>();
        ArrayList<User> more18 = new ArrayList<>();

        report.put("Menores de edad: ", less18);
        report.put("Mayores de edad: ", more18);

        return userService.getUsers()
                .flatMap(user -> {
                    int age = user.getAge();
                    if (age >= 18) {
                        return Mono.just(user)
                                .doOnNext(more18::add)
                                .then();
                    } else {
                        return Mono.just(user)
                                .doOnNext(less18::add)
                                .then();
                    }
                })
                .then(Mono.just(ResponseEntity.ok().body(report)));
    }



}
