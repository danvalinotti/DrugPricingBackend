package com.galaxe.drugpriceapi.src.TableModels;

import com.galaxe.drugpriceapi.src.TableModels.listeners.ProfileListener;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@EntityListeners(ProfileListener.class)
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile {//USED
    @Id
    @GeneratedValue
    int id;
    @Column
    String name;
    @Column
    String username;
    @Column
    String password;
    @Column
    String role;
    @Column
    String activeToken;
    @Temporal(TemporalType.TIMESTAMP)
    Date tokenDate;

}
