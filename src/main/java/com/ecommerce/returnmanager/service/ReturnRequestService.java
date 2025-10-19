package com.ecommerce.returnmanager.service;

import java.util.List;
import java.util.Optional;

import com.ecommerce.returnmanager.model.ReturnRequest;

public interface ReturnRequestService {

    /**
     * Initiates a new return request by a customer.
     * Includes business logic validation (e.g., 30-day window check).
     * @param request The ReturnRequest object populated with order and user data.
     * @return The saved ReturnRequest.
     */
    ReturnRequest initiateReturn(ReturnRequest request);

    /**
     * Retrieves all pending return requests for an admin review.
     * @return List of ReturnRequest objects.
     */
    List<ReturnRequest> getAllPendingRequests();

    /**
     * Approves a pending return request.
     * Updates status to APPROVED and handles refund calculation (simulated).
     * @param requestId ID of the request to approve.
     * @param adminNotes Notes from the admin processing the request.
     * @return The updated ReturnRequest.
     */
    ReturnRequest approveRequest(Long requestId, String adminNotes);

    /**
     * Rejects a pending return request.
     * Updates status to REJECTED.
     * @param requestId ID of the request to reject.
     * @param adminNotes Notes explaining the rejection reason.
     * @return The updated ReturnRequest.
     */
    ReturnRequest rejectRequest(Long requestId, String adminNotes);

    /**
     * Retrieves a single return request by ID.
     * @param id The ID of the request.
     * @return An Optional containing the request or empty if not found.
     */
    Optional<ReturnRequest> getRequestById(Long id);
}