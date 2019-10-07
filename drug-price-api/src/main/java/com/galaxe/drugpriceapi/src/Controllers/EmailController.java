package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Helpers.EmailConfig;
import com.galaxe.drugpriceapi.src.Repositories.AlertTypeRepository;
import com.galaxe.drugpriceapi.src.TableModels.Alert;
import com.galaxe.drugpriceapi.src.TableModels.AlertType;
import com.galaxe.drugpriceapi.src.TableModels.Profile;
import com.galaxe.drugpriceapi.src.Repositories.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


@CrossOrigin
@RestController
public class EmailController {

    EmailConfig emailConfig = new EmailConfig();
    @Autowired
    AlertTypeRepository alertTypeRepository;
    @Autowired
    ProfileRepository profileRepository;


    public void sendEmailViaAlert(Alert alert) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        try {
            AlertType alertType = alertTypeRepository.findById(Integer.parseInt(alert.getAlertTypeId())).get();
            List<String> stringIds = Arrays.asList(alertType.getRecipients().split(","));
            mailSender.setHost(emailConfig.getHost());
            mailSender.setPort(emailConfig.getPort());
            mailSender.setUsername(emailConfig.getUsername());
            mailSender.setPassword(emailConfig.getPassword());

            for (String s : stringIds) {
                Profile profile = profileRepository.findById(Integer.parseInt(s)).get();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("alerts@galaxe.com");
                message.setTo(profile.getUsername());
                message.setSubject(alert.getName());
                message.setText(alertType.getHeader() + "\n" + alertType.getSummary() + "\n" + alert.getDetailedMessage() + "\n" +
                        alertType.getFooter());
            }
        }catch (Exception ex){

        }
    }

    public void sendEmailViaProfile(Profile profile) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        try {

            mailSender.setHost(emailConfig.getHost());
            mailSender.setPort(emailConfig.getPort());
            mailSender.setUsername(emailConfig.getUsername());
            mailSender.setPassword(emailConfig.getPassword());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("RxWave@galaxe.com");
            message.setTo(profile.getUsername());
            message.setSubject("Rx Wave Account Confirmation");
            String text = "Hello " + profile.getName() + ",\n \t An account was created for you for RxWave. Your password is "+profile.getPassword()+". You may access the site by going to https://rxwave.galaxe.com and logging in with this email. If you have not requested or created this account, " +
                    "please contact rxWave@galaxe.com. \n Have a great day, \n RxWave";
            message.setText(text);
            mailSender.send(message);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
