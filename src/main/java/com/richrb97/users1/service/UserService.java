package com.richrb97.users1.service;

import com.richrb97.users1.document.User;
import com.richrb97.users1.repository.UserRepository;
import com.richrb97.users1.utils.ExportToExcel;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Flux<User>  getUsers(){
        return userRepository.findAll();
    }

    public Mono<User> create(User user){
        return userRepository.save(user);
    }

    public Mono<User> findUserById(String id){
        return userRepository.findById(id);
    }

    public Mono<User> updateUserById(String id, User user) {
        return userRepository.findById(id)
                .flatMap(userOld -> {
                    userOld.setName(user.getName());
                    userOld.setAge(user.getAge());
                    return userRepository.save(userOld);
                });
    }

    public Flux<User> usersByAgeRange(int minAge, int maxAge) {
       return userRepository.findAll()
               .filter(user -> minAge < user.getAge() && user.getAge() < maxAge);
    }

    public Flux<User> usersByTypeAddress(String typeAddress){
        return userRepository.findAll()
                .filter(user -> user.getAddress().split(" ")[0].equals(typeAddress));
    }

    public Mono<ResponseEntity<ByteArrayResource>> exportUsersToExcel() {
        return userRepository.findAll()
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .collectList()
                .map(this::generateExcelAndPrepareResponse)
                .onErrorResume(Mono::error);
    }

    private ResponseEntity<ByteArrayResource> generateExcelAndPrepareResponse(List<User> users) {
        try {
            byte[] excelBytes = ExportToExcel.exportUsers(Flux.fromIterable(users));

            ByteArrayResource resource = new ByteArrayResource(excelBytes);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=users.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Error while generating Excel", e);
        }
    }

}
