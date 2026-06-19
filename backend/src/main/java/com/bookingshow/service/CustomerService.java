package com.bookingshow.service;

import com.bookingshow.dto.CustomerRequest;
import com.bookingshow.dto.CustomerResponse;
import com.bookingshow.entity.Customer;
import com.bookingshow.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse registerCustomer(CustomerRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        Customer saved = customerRepository.save(customer);
        return convertToResponse(saved);
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        return convertToResponse(customer);
    }

    private CustomerResponse convertToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .build();
    }
}