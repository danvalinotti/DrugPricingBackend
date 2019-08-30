package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import org.hibernate.query.criteria.internal.SelectionImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    public void sendEmailToMany(List<String> recipients){

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfig.getHost());
        mailSender.setPort(emailConfig.getPort());
        mailSender.setUsername(emailConfig.getUsername());
        mailSender.setPassword(emailConfig.getPassword());

        for (String recipient: recipients) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("alerts@galaxe.com");
            message.setTo(recipient);
            message.setSubject("Alert");
            message.setText("Testing email");

//            mailSender.send(message);
        }


    }
    @GetMapping("/email/send")
    public StringSender sendEmail(){
        try{
            List<String> emails = new ArrayList<>();
            emails.add("mgood@galaxe.com");
//            emails.add("test@galaxe.com");
            sendEmailToMany(emails);
            StringSender sender = new StringSender();
            sender.setKey("Email");
            sender.setValue("Email Sent Successfully");
            return sender;
        }catch (Exception ex){
            ex.printStackTrace();
            StringSender sender = new StringSender();
            sender.setKey("Email");
            sender.setValue("Email Failed");
            return sender;
        }
    }

    public void sendEmailViaAlert(Alert alert) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        try {
            AlertType alertType = alertTypeRepository.findById(Integer.parseInt(alert.getAlertTypeId())).get();
            List<String> stringIds = Arrays.asList(alertType.getRecipients().split(","));
//        List<Integer> ids = new ArrayList<>();
            List<Profile> profiles = new ArrayList<>();
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
//                mailSender.send(message);
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
