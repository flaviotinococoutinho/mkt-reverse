package com.marketplace.proposal.domain.service;

import com.marketplace.proposal.domain.command.SubmitProposalCommand;
import com.marketplace.proposal.domain.exception.InvalidProposalException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Chain of Responsibility for proposal validation.
 * 
 * Validates proposals through a chain of validators.
 * Each validator can add validation errors.
 * 
 * Follows:
 * - Chain of Responsibility Pattern
 * - Template Method Pattern
 * - Functional Programming (Consumer)
 */
public class ProposalValidationChain {
    
    private final List<Consumer<ValidationContext>> validators;
    
    public ProposalValidationChain() {
        this.validators = new ArrayList<>();
        setupValidators();
    }
    
    /**
     * Validates submit proposal command.
     * 
     * @param command submit proposal command
     * @throws InvalidProposalException if validation fails
     */
    public void validate(SubmitProposalCommand command) {
        ValidationContext context = new ValidationContext(command);
        
        validators.forEach(validator -> validator.accept(context));
        
        if (context.hasErrors()) {
            throw new InvalidProposalException(context.getErrors());
        }
    }
    
    private void setupValidators() {
        validators.add(this::validatePrice);
        validators.add(this::validateDeliveryTime);
        validators.add(this::validateDescription);
        validators.add(this::validateAttachments);
    }
    
    private void validatePrice(ValidationContext context) {
        SubmitProposalCommand command = context.getCommand();
        
        if (command.price().isZero()) {
            context.addError("Price cannot be zero");
        }
        
        if (command.price().isNegative()) {
            context.addError("Price cannot be negative");
        }
    }
    
    private void validateDeliveryTime(ValidationContext context) {
        SubmitProposalCommand command = context.getCommand();
        
        if (command.deliveryTime().totalHours() == 0) {
            context.addError("Delivery time cannot be zero");
        }
        
        if (command.deliveryTime().days() > 365) {
            context.addError("Delivery time cannot exceed 365 days");
        }
    }
    
    private void validateDescription(ValidationContext context) {
        SubmitProposalCommand command = context.getCommand();
        String description = command.description();
        
        if (description.isBlank()) {
            context.addError("Description cannot be blank");
        }
        
        if (description.length() < 50) {
            context.addError("Description must be at least 50 characters");
        }
        
        if (description.length() > 5000) {
            context.addError("Description cannot exceed 5000 characters");
        }
    }
    
    private void validateAttachments(ValidationContext context) {
        SubmitProposalCommand command = context.getCommand();
        
        if (command.attachments().size() > 10) {
            context.addError("Cannot have more than 10 attachments");
        }
        
        command.attachments().forEach(attachment -> {
            if (attachment.isBlank()) {
                context.addError("Attachment URL cannot be blank");
            }
        });
    }
    
    /**
     * Validation context holding command and errors.
     */
    public static class ValidationContext {
        private final SubmitProposalCommand command;
        private final List<String> errors;
        
        public ValidationContext(SubmitProposalCommand command) {
            this.command = command;
            this.errors = new ArrayList<>();
        }
        
        public SubmitProposalCommand getCommand() {
            return command;
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return List.copyOf(errors);
        }
    }
}
