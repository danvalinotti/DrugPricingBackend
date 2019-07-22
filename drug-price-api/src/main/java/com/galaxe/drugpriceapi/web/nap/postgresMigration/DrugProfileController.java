package com.galaxe.drugpriceapi.web.nap.postgresMigration;

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
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Price;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.PricesAndMaster;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Profile;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@CrossOrigin
@RestController
public class DrugProfileController {

    @Autowired
    ProfileRepository profileRepository;


   ///PROFILES

    @PostMapping(value = "/admin/create/user")
    public Profile adminCreateUser(@RequestBody Profile profile)  {
        if(profileRepository.findByUsername(profile.getUsername()).size() != 0){
            String newPassword = BCrypt.hashpw("Galaxy123",BCrypt.gensalt());
            profile.setPassword(newPassword);
            profile.setRole("created"+profile.getRole());
            return profileRepository.save(profile);
        }else{
            profile.setUsername("Exists");
            return profile;
        }

    }
    @GetMapping(value = "/admin/get/users")
    public List<Profile> adminGetUsers()  {
        return profileRepository.findAll();
    }
    @PostMapping("/signUp")
    public Profile signUp(@RequestBody Profile profile){
        if(profileRepository.findByUsername(profile.getUsername()).size() == 0){
            String newPassword = BCrypt.hashpw(profile.getPassword(),BCrypt.gensalt());
            profile.setPassword(newPassword);
            return profileRepository.save(profile);
        }else{
            profile.setUsername("Exists");
            return profile;
        }

    }
}
