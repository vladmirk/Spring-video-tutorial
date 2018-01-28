package com.vk.learningspringboot.learningspringbootvideo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * Created by vkutn on 20/12/2017.
 */
@Entity
public class Image {

    @Id
    @GeneratedValue
    private long id;
    private String name;

    @OneToOne
    private User user;

    private Image() {
    }

    public Image(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
