package me.third.right.discordBotCLI.managers;

import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.events.UserAddRemoveEvent;
import me.third.right.discordBotCLI.hacks.DiscordBotCLI;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.discordBotCLI.utils.enums.Result;
import me.third.right.discordBotCLI.utils.enums.Type;
import me.third.right.utils.client.objects.Pair;
import me.third.right.utils.client.utils.FileUtils;
import me.third.right.utils.client.utils.LoggerUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;

import static me.third.right.discordBotCLI.utils.Tools.stringToEnum;


public class AuthManagement {
    private final Path folderPath = DiscordBotCLI.INSTANCE.path.resolve("Authority");
    private final HashSet<Pair<Long, Authority>> userList = new HashSet<>();
    private boolean isLoading = false;

    public AuthManagement() {
        FileUtils.folderExists(folderPath);
        loadData();
    }

    // Methods

    public void saveData() {
        if(isLoading) return;
        FileUtils.folderExists(folderPath);

        final JSONArray dataArray = new JSONArray();
        for(Pair<Long, Authority> user : userList) {
            final JSONObject object = new JSONObject();
            object.put("UID", user.getFirst());
            object.put("AUTH", user.getSecond().toString());
            dataArray.add(object);
        }

        try (FileWriter file = new FileWriter(folderPath.resolve("authData.json").toFile())) {
            file.write(dataArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        userList.clear();
        isLoading = true;
        if(!Files.exists(folderPath.resolve("authData.json"))) {
            LoggerUtils.moduleLog(DiscordBotCLI.INSTANCE,"No authData.json found, creating new one.");
            isLoading = false;
            saveData();
            return;
        }

        final JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(folderPath.resolve("authData.json").toFile())) {
            final Object obj = jsonParser.parse(reader);

            JSONArray dataArray = (JSONArray) obj;
            for(Object object : dataArray) {
                final JSONObject jsonObject =  (JSONObject) object;
                final long uid = (Long) jsonObject.get("UID");
                final Authority authority = stringToEnum(Authority.class, (String)jsonObject.get("AUTH"));
                addUser(new Pair<>(uid, authority));
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        isLoading = false;
    }

    public Result addUser(Pair<Long, Authority> user) {
        if (replicaCheck(user)) return Result.FAILURE;
        userList.add(user);
        if(isLoading) return Result.SUCCESS;
        ThirdMod.EVENT_PROCESSOR.post(new UserAddRemoveEvent(user, Type.Add));
        return Result.SUCCESS;
    }

    public Result removeUser(long uid) {
        final Pair<Long, Authority> pair = getByID(uid);
        if(pair == null) return Result.FAILURE;
        userList.remove(pair);
        ThirdMod.EVENT_PROCESSOR.post(new UserAddRemoveEvent(pair, Type.Remove));
        return Result.SUCCESS;
    }

    private boolean replicaCheck(Pair<Long, Authority> user) {
        for(Pair<Long, Authority> i : userList) {
            if(Objects.equals(user.getFirst(), i.getFirst())) {
                return true;
            }
        }
        return false;
    }

    public Pair<Long, Authority> getByID(long id) {
        for(Pair<Long, Authority> i : userList) {
            if(i.getFirst() == id) {
                return i;
            }
        }
        return null;
    }

    public HashSet<Pair<Long, Authority>> getUserList() {
        return userList;
    }
}
