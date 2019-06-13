package com.galaxe.drugpriceapi.web.nap.controller;


import java.time.ZonedDateTime;

public class Node{
    ZonedDateTime data;
    Node next;
    public Node(ZonedDateTime data) {
        this.data = data;
    }
}