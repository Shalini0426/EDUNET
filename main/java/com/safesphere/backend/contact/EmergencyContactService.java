package com.safesphere.backend.contact;

import com.safesphere.backend.account.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmergencyContactService {

    private final EmergencyContactRepository contacts;
    private final UserAccountService accounts;

    public EmergencyContactService(EmergencyContactRepository contacts, UserAccountService accounts) {
        this.contacts = contacts;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<EmergencyContact> findAll(String ownerEmail) {
        return contacts.findAllByOwnerEmailIgnoreCaseOrderByIdDesc(ownerEmail);
    }

    @Transactional(readOnly = true)
    public EmergencyContact findById(String ownerEmail, Long id) {
        return requireOwned(ownerEmail, id);
    }

    @Transactional
    public EmergencyContact create(String ownerEmail, String name, String phoneNumber, String relationship) {
        EmergencyContact contact = new EmergencyContact();
        contact.setOwner(accounts.requireByEmail(ownerEmail));
        apply(contact, name, phoneNumber, relationship);
        return contacts.save(contact);
    }

    @Transactional
    public EmergencyContact update(String ownerEmail, Long id, String name, String phoneNumber, String relationship) {
        EmergencyContact contact = requireOwned(ownerEmail, id);
        apply(contact, name, phoneNumber, relationship);
        return contacts.save(contact);
    }

    @Transactional
    public void delete(String ownerEmail, Long id) {
        contacts.delete(requireOwned(ownerEmail, id));
    }

    private EmergencyContact requireOwned(String ownerEmail, Long id) {
        return contacts.findByIdAndOwnerEmailIgnoreCase(id, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Emergency contact not found."));
    }

    private void apply(EmergencyContact contact, String name, String phoneNumber, String relationship) {
        if (name == null || name.isBlank() || phoneNumber == null || phoneNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and phone number are required.");
        }
        contact.setName(name.trim());
        contact.setPhoneNumber(phoneNumber.trim());
        contact.setRelationship(relationship == null || relationship.isBlank() ? null : relationship.trim());
    }
}
