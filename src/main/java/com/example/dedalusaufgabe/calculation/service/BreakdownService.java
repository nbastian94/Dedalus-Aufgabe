package com.example.dedalusaufgabe.calculation.service;

import com.example.dedalusaufgabe.calculation.model.DenominationCount;
import com.example.dedalusaufgabe.calculation.model.EuroDenomination;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BreakdownService {

    public List<DenominationCount> calculateBreakdown(long amountInCents) {
        long remaining = amountInCents;
        List<DenominationCount> result = new ArrayList<>();

        for (EuroDenomination denomination : EuroDenomination.descending()) {
            int count = (int) (remaining / denomination.cents());
            if (count > 0) {
                result.add(new DenominationCount(denomination, count));
                remaining -= (long) count * denomination.cents();
            }
        }

        return result;
    }
}
