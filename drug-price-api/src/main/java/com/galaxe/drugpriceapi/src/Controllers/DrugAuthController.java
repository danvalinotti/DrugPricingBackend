package com.galaxe.drugpriceapi.src.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.galaxe.drugpriceapi.src.TableModels.Profile;
import com.galaxe.drugpriceapi.src.Repositories.ProfileRepository;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.*;

@CrossOrigin
@RestController
public class DrugAuthController {

    @Autowired
    ProfileRepository profileRepository;

    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    //AUTHORIZATION
    //----------------------------------------------------------
    @PostMapping("/create/token")
    public Profile createToken(@RequestBody Profile profile) {
        try {
            Profile profile1 = profileRepository.findByUsername(profile.getUsername()).get(0);
            if (BCrypt.checkpw(profile.getPassword(), profile1.getPassword()) == true) {
                ObjectMapper objectMapper = new ObjectMapper();
                String profileJson = "";
                try {
                    profileJson = objectMapper.writeValueAsString(profile);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                String jws = Jwts.builder().setSubject(profileJson).signWith(this.key).compact();
                Profile p = new Profile();
                Profile foundProfile = profileRepository.findByUsername(profile.getUsername()).get(0);
                foundProfile.setTokenDate(new Date());
                foundProfile.setActiveToken(jws);
                profileRepository.save(foundProfile);
                p.setName(jws);
                return p;
            } else {
                profile.setPassword("false");
                return profile;
            }
        }catch (Exception ex){
            profile.setPassword("false");
            return profile;
        }


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