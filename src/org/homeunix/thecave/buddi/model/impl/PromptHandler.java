package org.homeunix.thecave.buddi.model.impl;

public interface PromptHandler {
    /**
     * Ask the user a question with options.
     * 
     * @param message       The message to display.
     * @param title         The title of the dialog.
     * @param options       The options to present.
     * @param defaultOption The default option.
     * @return The index of the selected option.
     */
    int askUser(String message, String title, String[] options, String defaultOption);

    /**
     * Show a message to the user.
     * 
     * @param message The message to display.
     */
    void showMessage(String message);
}
