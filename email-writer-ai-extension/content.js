//This file helps to inject the button into Gmail's UI and make the API call to the server
//and get the response from the server and display it in the UI
console.log("Email Writer Extension - Content Script loaded!!!!");

//Function to inject the AI Reply Button into the Gmail UI
function injectAiReplyButton(){

    //Creating a button which looks similar to Gmail's UI
       //If the button already exists, then remove it
    const existingButton = document.querySelector('.ai-reply-button');
    if(existingButton){
        existingButton.remove();
    }

    const toolbar = findComposeToolbar();
    if(!toolbar){
        console.log("Compose Toolbar not found");
        return;
    }

    console.log("Compose Toolbar found, creating the AI Reply Button");

    const button = createAiButton();
    button.classList.add('ai-reply-button');

    button.addEventListener('click', async () => {
        console.log("AI Reply Button Clicked");
        //Make an API call to the backend server
        //Get the response from the server
        //Display the response in the UI
        try {
            button.innerHTML = 'Generating...';
            button.disabled = true;

            const emailContent = getEmailContent();
            const response = await fetch('http://localhost:8080/api/email/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    emailContent: emailContent,
                    tone: "professional"
                })

            });
            if(!response.ok){
                throw new Error('Failed to generate AI Reply. Please try again later.');
            }

            const generatedReply = await response.text();

            const composeBox = document.querySelector('[role="textbox"][g_editable="true"]');
            if(composeBox){
                composeBox.focus();
                document.execCommand('insertText', false, generatedReply);
                //execCommand() is a programatic way to mimic user actions. It is used to insert text into the compose box
            } else{
                console.error("Compose box not found");
            }
        } catch (error) {
            console.error(error);
            alert("Failed to generate Reply.");
        } finally{
            button.innerHTML = 'Reply using AI';
            button.disabled = false;
        }
    });

    //Injecting the button into the Gmail UI
    toolbar.insertBefore(button, toolbar.firstChild);

    //When the button is clicked, make an API call to the backend server

}

//Function to find the Compose Toolbar in Gmail
function findComposeToolbar(){
    const selectors = [
        '.btC',
        '.aDh',
        '[role="toolbar"]', //generic toolbar selector
        '.gU.Up' //alternative toolbar class that Gmail might use
    ];
    for(const selector of selectors){
        const toolbar = document.querySelector(selector);
        if(toolbar){
            return toolbar;
        }
        return null;
    }
}

//Function to create the AI Reply Button
function createAiButton(){
    const button = document.createElement('div');
    button.className = 'T-I J-J5-Ji aoO v7 T-I-atl L3';
    button.style.marginRight = '8px';
    button.innerText = 'Reply using AI';
    button.setAttribute('role', 'button');
    button.setAttribute('data-tooltip', 'Generate AI Reply');
    return button;
}

//Function to get the email content
function getEmailContent(){
    const selectors = [
        '.h7',
        '.a3s.aiL',
        '.gmail_quote',
        '[role="presentation"]'
    ];
    for(const selector of selectors){
        const emailContent = document.querySelector(selector);
        if(emailContent){
            return emailContent.innerText.trim();
        }
        return '';
    }
}

//Mutation Observer to observe the changes in the DOM tree
//Provides the ability to watch for changes being made to the DOM tree
//Refer to the following link : https://developer.mozilla.org/en-US/docs/Web/API/MutationObserver
const observer = new MutationObserver((mutations) => {
    for(const mutation of mutations){
        const addedNodes = Array.from(mutation.addedNodes);
        const hasComposeElements = addedNodes.some(node => 
            node.nodeType === Node.ELEMENT_NODE &&
            (node.matches('.aDh, .btC, [role="dialog"]') || node.querySelector('.aDh, .btC', '[role="dialog"]'))
        );

        if(hasComposeElements){
            console.log("Compose Window Detected");
            setTimeout(injectAiReplyButton, 500);
           
        }
    }
    
});

//Observe the entire body of the document
observer.observe(document.body, {
    childList: true,
    subtree: true
});