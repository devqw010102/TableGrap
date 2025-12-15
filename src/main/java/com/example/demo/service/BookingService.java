package com.example.demo.service;

import com.example.demo.data.dto.BookDto;
import com.example.demo.data.model.Book;

public interface BookingService {
    Book createBooking(BookDto bookDto);
}
