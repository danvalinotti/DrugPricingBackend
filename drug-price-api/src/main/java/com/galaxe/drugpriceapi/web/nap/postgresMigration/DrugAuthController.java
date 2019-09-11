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
import java.util.*;
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
       Profile foundProfile =  profileRepository.findByUsername(profile.getUsername()).get(0);
        foundProfile.setTokenDate(new Date());
        foundProfile.setActiveToken(jws);
        profileRepository.save(foundProfile);
        p.setName(jws);
        return p;

    }

    @PostMapping("/authenticate/token")
    public Profile authenticateToken(@RequestBody Profile profile) {

        try{

            List<Profile> profiles =  profileRepository.findByActiveToken(profile.getName());
           if(profiles.size()!=0){
               Profile foundProfile = profiles.get(0);
               profile.setPassword(profile.getName());
               profile.setUsername(foundProfile.getUsername());
               profile.setRole(foundProfile.getRole());
               foundProfile.setTokenDate(new Date());
               profileRepository.save(foundProfile);
               return profile;

           }else{
            profile.setPassword("false");
            return profile;
            }
        }catch (Exception ex){
            profile.setPassword("false");
            return profile;
        }

    }
    @PostMapping("/profile/logout")
    public Profile logout(@RequestBody Profile profile) {

        try{

            List<Profile> profiles =  profileRepository.findByActiveToken(profile.getName());
            if(profiles.size()!=0){
                Profile foundProfile = profiles.get(0);
//                profile.setPassword(profile.getName());
//                profile.setUsername(foundProfile.getUsername());
//                profile.setRole(foundProfile.getRole());
                foundProfile.setActiveToken(null);
                foundProfile.setTokenDate(null);
                profileRepository.save(foundProfile);
                return profile;

            }else{
                profile.setPassword("false");
                return profile;
            }
        }catch (Exception ex){
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