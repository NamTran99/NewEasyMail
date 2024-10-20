package com.hungbang.email2018.f.c;


import androidx.annotation.NonNull;


public class a {
    // account email
    @NonNull
    public String a;
    // accountType
    public int b;
    // signature
    public String i;
    // password
    public String h;

    public a(@NonNull String accMail, int accType, @NonNull String password, @NonNull String signature) {
        a = accMail;
        b = accType;
    }


    public String getA() {
        return a;
    }

    public void setA(@NonNull String a) {
        this.a = a;
    }


    /**
     * Get the real folder name of an account corresponding to a label
     *
     * @param label example : label = SPAM --> return Junk
     * @return
     */
    public String getRealFolderName(String label) {
        if (label == null) {
            return "";
        }
        // TODO: 2/7/2018
//        switch (label) {
//            case MailHelper.label.INBOX:
//                return folderNameInbox;
//            case MailHelper.label.SENT:
//                return folderNameSent;
//            case MailHelper.label.SPAM:
//                return folderNameSpam;
//            case MailHelper.label.DRAFT:
//                return folderNameDraft;
//            case MailHelper.label.TRASH:
//                return folderNameTrash;
//        }
        return label;
    }

//    @Nullable
//    public EmailProvidersWrapper.Provider getProvider() {
//        String domain = "";
//        switch (accountType) {
//            case AccountManager.AccountType.GOOGLE:
//                domain = "gmail.com";
//                break;
//            case AccountManager.AccountType.OUTLOOK:
//                domain = "outlook.com";
//                break;
//            case AccountManager.AccountType.YANDEX:
//                domain = "yandex.com";
//                break;
//            default:
//                domain = accountEmail;
//        }
//        return EmailServiceProviderHelper.getInstance().getProvider(domain);
//    }

    public String getI() {
        return i;
    }

    public void setI(String i) {
        this.i = i;
    }
}
