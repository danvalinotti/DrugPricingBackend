package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.TableModels.Profile;
import com.galaxe.drugpriceapi.src.Repositories.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@CrossOrigin
@RestController
public class DrugProfileController {

    @Autowired
    ProfileRepository profileRepository;

   ///PROFILES

    @PostMapping(value = "/admin/create/user")
    public Profile adminCreateUser(@RequestBody Profile profile)  {

            try {
                profile.setUsername(profile.getUsername().trim());
            }catch (Exception ex){

            }
            profile.setUsername(profile.getUsername().trim());
            profile.setRole("created"+profile.getRole());
            return profileRepository.save(profile);

    }

    @GetMapping(value = "/admin/get/users")
    public List<Profile> adminGetUsers()  {
        return profileRepository.findAll();
    }

    @PostMapping("/signUp")
    public Profile signUp(@RequestBody Profile profile){
        if(profileRepository.findByUsername(profile.getUsername()).size() == 0){
            try {
                profile.setUsername(profile.getUsername().trim());
            }catch (Exception ex){

            }
            String newPassword = BCrypt.hashpw(profile.getPassword(),BCrypt.gensalt());
            profile.setPassword(newPassword);
            return profileRepository.save(profile);
        }else{
            profile.setUsername("Exists");
            return profile;
        }

    }

}
