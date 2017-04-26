package net.kuama.backgroundservice;

class CertificateData {
    private String certFilePath;
    private String certFilePassword;

    CertificateData(String path, String pwd) {
        certFilePassword = pwd;
        certFilePath = path;
    }

    public String getPath() {
        return certFilePath;
    }

    public String getPassword() {
        return certFilePassword;
    }
}
