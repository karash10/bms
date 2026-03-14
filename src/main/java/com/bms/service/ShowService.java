package com.bms.service;

import com.bms.dto.ShowRequest;
import com.bms.entity.Show;

public interface ShowService {

    Show createShow(ShowRequest request);

    Show getShow(Long id);
}