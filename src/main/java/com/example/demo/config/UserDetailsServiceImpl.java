package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.demo.Entity.User;
import com.example.demo.dao.UserRepository;

public class UserDetailsServiceImpl implements UserDetailsService{


	@Autowired
	UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException 
	{
		User user=userRepository.loadUserByUsername(username);
		if(user==null)
		{
			throw new UsernameNotFoundException("Could not found usser!!");
		}
		CustomeUserDetails customeUserDetails=new CustomeUserDetails(user);
		return customeUserDetails;
	}
	
	
}
