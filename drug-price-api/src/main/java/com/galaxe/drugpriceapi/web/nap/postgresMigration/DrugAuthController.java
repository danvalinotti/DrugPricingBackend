package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxe.drugpriceapi.model.DrugNAP2;
import com.galaxe.drugpriceapi.model.InsideRx;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient2;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient3;
import com.galaxe.drugpriceapi.web.nap.controller.PriceController;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterListTestController;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@CrossOrigin
@RestController
public class DrugAuthController {

    @Autowired
    ProfileRepository profileRepository;

    int count = 0;
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);


    //AUTHORIZATION
    //----------------------------------------------------------
    @PostMapping("/create/token")
    public Profile createToken(@RequestBody Profile profile) {

        ObjectMapper objectMapper = new ObjectMapper();
        String profileJson = "";
        try {
            profileJson = objectMapper.writeValueAsString(profile);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String jws = Jwts.builder().setSubject(profileJson).signWith(this.key).compact();
        Profile p = new Profile();
        p.setName(jws);
        return p;

    }

    @PostMapping("/authenticate/token")
    public Profile authenticateToken(@RequestBody Profile profile) {

        try {
            String s = Jwts.parser().setSigningKey(this.key).parseClaimsJws(profile.getName()).getBody().getSubject();
            ObjectMapper objectMapper = new ObjectMapper();
            Profile p = objectMapper.readValue(s, Profile.class);
            Profile profile1 = profileRepository.findByUsername(p.getUsername()).get(0);
            boolean isCorrect = BCrypt.checkpw(p.getPassword(),profile1.getPassword());
            if(isCorrect==true){
                profile.setPassword(profile.getName());
                profile.setUsername(profile1.getUsername());
                profile.setRole(profile1.getRole());
                return profile;
            }else{
                profile.setPassword("false");
                return profile;
            }


        } catch (Exception e) {
            profile.setPassword("false");
            return profile;
        }

    }
    @PostMapping("/update/password")
    public Profile updatePassword(@RequestBody Profile profile){
       Profile p = profileRepository.findByUsername(profile.getUsername()).get(0);
       boolean isCorrect = BCrypt.checkpw(profile.getPassword(),p.getPassword());
       if(isCorrect == true){
           String newPassword = BCrypt.hashpw(profile.getRole(),BCrypt.gensalt());
           p.setPassword(newPassword);
           String role = p.getRole();
           if(role.equals("createduser")){
               role = "user";
           }
           if(role.equals("createdadmin")){
               role = "admin";
           }

           p.setRole(role);
           profileRepository.save(p);
       }else{
           return null;
       }
       return null;
    }

}