package com.example.demo.dao;

import javax.websocket.server.PathParam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer>
{
	//here we are giving pagination for 
	//in pagable variable is stored currentPage-page and Contact per page - 5
		@Query("from Contact as c WHERE c.user.id= :userId")
		public Page<Contact> getContactsByUser(@PathParam("userId") int userId,Pageable pageable);
}
