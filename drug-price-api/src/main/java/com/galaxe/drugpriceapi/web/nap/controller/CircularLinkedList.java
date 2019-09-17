package com.galaxe.drugpriceapi.web.nap.controller;

import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

public class CircularLinkedList {
    public Node head;
    public Node tail;

    public CircularLinkedList(){
        this.head =null;
        this.tail = null;
    }

    //This function will add the new node at the end of the list.
    public void add(ZonedDateTime data){
        //Create new node
        Node newNode = new Node(data);
        //Checks if the list is empty.
        if(this.head == null) {
            //If list is empty, both head and tail would point to new node.
            this.head = newNode;
            this.tail = newNode;
            newNode.next = head;
        }
        else {
            //tail will point to new node.
            this.tail.next = newNode;
            //New node will become new tail.
            this.tail = newNode;
            //Since, it is circular linked list tail will point to head.
            this.tail.next = head;
        }
    }
    //Represents the node of list.

}
