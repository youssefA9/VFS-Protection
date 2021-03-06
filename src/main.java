import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class main {

    public static void updateSystemFile(FileWriter VFS, Directory Dir) throws IOException {
        VFS.write("<" + Dir.name + ">" + "\n");
        for (int i = 0; i < Dir.files.size(); i++) {
            if (Dir.files.get(i).deleted == false) {
                VFS.write(Dir.files.get(i).name + "\t" + Dir.files.get(i).size + "\n");
            }
        }
        for (int i = 0; i < Dir.subDirectories.size(); i++) {
            if (Dir.subDirectories.get(i).deleted == false) {
                updateSystemFile(VFS, Dir.subDirectories.get(i));
            }
        }
        VFS.write("$\n");
    }

    public static void loadSystemFile(Scanner sc, String path, system sys, Directory Dir) {
        String[] temp;
        user current = new user("admin", "admin");
        while (sc.hasNext()) {
            temp = sc.nextLine().split("\t");
            if (temp[0].charAt(0) != '$') {
                if (temp[0].charAt(0) != '<') {
                    sys.createfile(path + temp[0], Integer.valueOf(temp[1]), false, "");
                } else {
                    sys.createfolder(path + temp[0].substring(1, temp[0].length() - 1), false, current);
                    loadSystemFile(sc, path + temp[0].substring(1, temp[0].length() - 1) + "/", sys, Dir.subDirectories.get(Dir.subDirectories.size() - 1));
                }
            } else {
                return;
            }
        }
    }


    public static void updateUserFile(FileWriter userFile, ArrayList<user> users) throws IOException {
        for (int i = 0; i < users.size(); i++) {
            userFile.write(users.get(i).username + "," + users.get(i).password + "\n");

        }
    }

    public static void loadUserFile(Scanner sc, ArrayList<user> users) {
        String[] line;
        while (sc.hasNextLine()) {
            line = sc.nextLine().split(",");
            users.add(new user(line[0], line[1]));
        }
    }

    public static void updateCapFile(FileWriter capFile, ArrayList<user> users) throws IOException {
        for (int i = 0; i < users.size(); i++) {
            for (int j = 0; j < users.get(i).cap.size(); j++) {
                capFile.write(users.get(i).cap.get(j).path + "," + users.get(i).username + "," + users.get(i).cap.get(j).code);
                capFile.write("\n");
            }
        }
    }

    public static void loadCapFile(Scanner sc, ArrayList<user> users) {
        String[] line;
        while (sc.hasNextLine()) {
            line = sc.nextLine().split(",");
            for (int i = 1; i < line.length; i += 2) {
                for (int j = 0; j < users.size(); j++) {
                    if (users.get(j).username.equals(line[i])) {
                        users.get(j).cap.add(new ability(line[0], Integer.valueOf(line[i + 1])));
                        break;
                    }
                }
            }
        }
    }


    public static void main(String args[]) throws IOException {
        File VFS_Read = new File("backupFile.txt");
        File usersBackupFile_Read = new File("user.txt");
        File capBackupFile_Read = new File("capabilities.txt");
        FileWriter VFS_Write;
        FileWriter usersBackupFile_Write;
        FileWriter capBackupFile_Write;
        Scanner cmd = new Scanner(System.in);
        Scanner in = new Scanner(System.in);
        String input = "";
        String[] cmds;
        ArrayList<user> users = new ArrayList<user>();

        Scanner fileSC;
        fileSC = new Scanner(usersBackupFile_Read);
        loadUserFile(fileSC, users);

        user current = users.get(0);
        system sys = new system();
        Allocation allocation;
        System.out.println("Enter Disk's number of blocks: ");
        int N = in.nextInt();
        System.out.println("Enter Allocation method: ");
        System.out.println("1- Contiguous Allocation");
        System.out.println("2- Indexed Allocation");
        System.out.println("3- Linked Allocation");
        int choice = in.nextInt();
        String t = "";
        switch (choice) {
            case 1:
                allocation = new Contiguous();
                sys = new system(N, allocation);
                t = "Contiguous";
                break;
            case 2:
                allocation = new Indexed();
                sys = new system(N, allocation);
                t = "Indexed";
                break;
            case 3:
                allocation = new Linked();
                sys = new system(N, allocation);
                t = "Linked";
                break;

        }

        fileSC = new Scanner(VFS_Read);
        if (fileSC.hasNext()) {
            String temp = fileSC.nextLine();
            temp = temp.substring(1, temp.length() - 1);
            if (temp.equals("root")) {
                loadSystemFile(fileSC, temp + "/", sys, sys.root);
            }
        }

        fileSC = new Scanner(capBackupFile_Read);
        loadCapFile(fileSC, users);


        String path = "";
        while (true) {
            System.out.println("Enter Command: ");
            input = cmd.nextLine();
            cmds = input.split(" ");

            if (cmds[0].equals("TellUser")) {
                if (cmds.length == 1) {
                    System.out.println("Current User: " + current.username);
                } else {
                    System.out.println("invalid command!");
                }

            } else if (cmds[0].equals("CUser")) {
                if (current.username.equals("admin")) {
                    if (cmds.length == 3) {
                        boolean flag = false;
                        for (int i = 0; i < users.size(); i++) {
                            if (users.get(i).username.equals(cmds[1])) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            users.add(new user(cmds[1], cmds[2]));

                            usersBackupFile_Write = new FileWriter("user.txt");
                            updateUserFile(usersBackupFile_Write, users);
                            usersBackupFile_Write.close();

                            System.out.println("User is created successfully!");
                        } else {
                            System.out.println("Username is already exists!");
                        }
                    } else {
                        System.out.println("Invalid attributes for command!");
                    }
                } else {
                    System.out.println("No access for this user to use this method!");
                }
            } else if (cmds[0].equals("Grant")) {
                if (current.username.equals("admin")) {
                    user x = null;
                    if (cmds.length == 4) {
                        for (int i = 0; i < users.size(); i++) {
                            if (users.get(i).username.equals(cmds[1])) {
                                x = users.get(i);
                                break;
                            }
                        }
                        if (x != null) {
                            if (!x.username.equals("admin")) {
                                boolean access = false;
                                ability a = new ability(cmds[2], Integer.parseInt(cmds[3]));
                                for (int i = 0; i < x.getCap().size(); i++) {
                                    if (x.getCap().get(i).path.equals(a.path)) {
                                        access = true;
                                        break;
                                    }
                                }
                                if (!access) {
                                    ArrayList<ability> cap = x.getCap();
                                    cap.add(a);
                                    x.setCap(cap);
                                    capBackupFile_Write = new FileWriter("capabilities.txt");
                                    updateCapFile(capBackupFile_Write, users);
                                    capBackupFile_Write.close();
                                    System.out.println("Capability has been Granted!");
                                } else {
                                    System.out.println("Folder is already has access from this user! ");
                                }
                            }
                        } else {
                            System.out.println("No user with this username!");
                        }
                    } else {
                        System.out.println("Invalid attributes for command!");
                    }
                } else {
                    System.out.println("No access for this user to use this method!");
                }
            } else if (cmds[0].equals("Login")) {
                user x = null;
                if (cmds.length == 3) {
                    for (int i = 0; i < users.size(); i++) {
                        if (users.get(i).username.equals(cmds[1])) {
                            x = users.get(i);
                            break;
                        }
                    }
                    if (x != null && x.password.equals(cmds[2])) {
                        current = x;
                        System.out.println("User is logged in!");
                    } else {
                        System.out.println("login is failed. Username or Password is wrong!");
                    }
                } else {
                    System.out.println("Invalid attributes for command!");
                }

            } else if (cmds[0].equals("CreateFile")) {
                if (cmds.length >= 3) {
                    sys.createfile(cmds[1], Integer.parseInt(cmds[2]), true, t);
                    VFS_Write = new FileWriter("backupFile.txt");
                    updateSystemFile(VFS_Write, sys.root);
                    VFS_Write.close();
                } else
                    System.out.println("wrong inputs!");
            } else if (cmds[0].equals("CreateFolder")) {
                sys.createfolder(cmds[1], true, current);
                VFS_Write = new FileWriter("backupFile.txt");
                updateSystemFile(VFS_Write, sys.root);
                VFS_Write.close();
            } else if (cmds[0].equals("DeleteFile")) {
                sys.deletefile(cmds[1]);
                VFS_Write = new FileWriter("backupFile.txt");
                updateSystemFile(VFS_Write, sys.root);
                VFS_Write.close();
            } else if (cmds[0].equals("DeleteFolder")) {
                sys.deletefolder(cmds[1], current);
                VFS_Write = new FileWriter("backupFile.txt");
                updateSystemFile(VFS_Write, sys.root);
                VFS_Write.close();
            } else if (cmds[0].equals("DisplayDiskStatus")) {
                sys.DisplayDiskStatus();
            } else if (cmds[0].equals("DisplayDiskStructure")) {
                sys.DisplayDiskStructure();
            } else if (cmds[0].equalsIgnoreCase("exit")) {
                VFS_Write = new FileWriter("backupFile.txt");
                updateSystemFile(VFS_Write, sys.root);
                VFS_Write.close();
                break;

            } else {
                System.out.println("Wrong Command!");
            }

        }


    }

}
