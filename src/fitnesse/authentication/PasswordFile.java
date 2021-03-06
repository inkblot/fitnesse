// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import util.FileUtil;
import util.TodoException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class PasswordFile {
    private File passwordFile;
    private Map<String, String> passwordMap = new HashMap<String, String>();
    private PasswordCipher cipher = new TransparentCipher();

    public PasswordFile(String filename) {
        passwordFile = new File(filename);
        loadFile();
    }

    public PasswordFile(String filename, PasswordCipher cipher) throws Exception {
        this(filename);
        this.cipher = cipher;
    }

    public Map<String, String> getPasswordMap() {
        return passwordMap;
    }

    public String getName() {
        return passwordFile.getName();
    }

    public PasswordCipher getCipher() {
        return cipher;
    }

    public void savePassword(String user, String password) throws Exception {
        passwordMap.put(user, cipher.encrypt(password));
        savePasswords();
    }

    private void loadFile() {
        LinkedList<String> lines = getPasswordFileLines();
        loadCipher(lines);
        loadPasswords(lines);
    }

    private void loadPasswords(LinkedList<String> lines) {
        for (String line : lines) {
            if (isNotEmpty(line)) {
                String[] tokens = line.split(":");
                passwordMap.put(tokens[0], tokens[1]);
            }
        }
    }

    private void loadCipher(LinkedList<String> lines) {
        if (lines.size() > 0) {
            String firstLine = lines.getFirst();
            if (firstLine.startsWith("!")) {
                String cipherClassName = firstLine.substring(1);
                instantiateCipher(cipherClassName);
                lines.removeFirst();
            }
        }
    }

    public PasswordCipher instantiateCipher(String cipherClassName) {
        try {
            Class<?> cipherClass = Class.forName(cipherClassName);
            Constructor<?> constructor = cipherClass.getConstructor(new Class[]{});
            cipher = (PasswordCipher) constructor.newInstance();
        } catch (InstantiationException e) {
            throw new TodoException(e);
        } catch (IllegalAccessException e) {
            throw new TodoException(e);
        } catch (InvocationTargetException e) {
            throw new TodoException(e);
        } catch (ClassNotFoundException e) {
            throw new TodoException(e);
        } catch (NoSuchMethodException e) {
            throw new TodoException(e);
        }
        return cipher;
    }

    private void savePasswords() throws Exception {
        List<String> lines = new LinkedList<String>();
        lines.add("!" + cipher.getClass().getName());
        for (String user : passwordMap.keySet()) {
            Object password = passwordMap.get(user);
            lines.add(user + ":" + password);
        }
        FileUtil.writeLinesToFile(passwordFile, lines);
    }

    private LinkedList<String> getPasswordFileLines() {
        LinkedList<String> lines = new LinkedList<String>();
        try {
            if (passwordFile.exists()) {
                lines = FileUtil.getFileLines(passwordFile);
            }
        } catch (IOException e) {
            throw new TodoException(e);
        }
        return lines;
    }
}
