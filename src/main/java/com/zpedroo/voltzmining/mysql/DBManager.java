package com.zpedroo.voltzmining.mysql;

import com.zpedroo.voltzmining.data.PlayerData;
import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.managers.PickaxeManager;
import com.zpedroo.voltzmining.objects.Enchant;
import com.zpedroo.voltzmining.objects.Pickaxe;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class DBManager {

    public void save(PlayerData data) {
        if (contains(data.getUUID().toString(), "uuid")) {
            String query = "UPDATE `" + DBConnection.TABLE + "` SET" +
                    "`uuid`='" + data.getUUID().toString() + "', " +
                    "`available_blocks`='" + data.getAvailableBlocks().toString() + "', " +
                    "`broken_blocks`='" + data.getBrokenBlocks().toString() + "', " +
                    "`pickaxe_material`='" + data.getPickaxe().getType().toString() + "', " +
                    "`pickaxe_name`='" + data.getPickaxe().getName() + "', " +
                    "`pickaxe_enchants`='" + serializeEnchants(data.getPickaxe().getEnchants()) + "' " +
                    "WHERE `uuid`='" + data.getUUID().toString() + "';";
            executeUpdate(query);
            return;
        }

        String query = "INSERT INTO `" + DBConnection.TABLE + "` (`uuid`, `available_blocks`, `broken_blocks`, `pickaxe_material`, `pickaxe_name`, `pickaxe_enchants`) VALUES " +
                "('" + data.getUUID().toString() + "', " +
                "'" + data.getAvailableBlocks().toString() + "', " +
                "'" + data.getBrokenBlocks().toString() + "', " +
                "'" + data.getPickaxe().getType().toString() + "', " +
                "'" + data.getPickaxe().getName() + "', " +
                "'" + serializeEnchants(data.getPickaxe().getEnchants()) + "');";
        executeUpdate(query);
    }

    public PlayerData load(Player player) {
        if (!contains(player.getUniqueId().toString(), "uuid")) {
            return new PlayerData(player.getUniqueId(), BigInteger.ZERO, BigInteger.ZERO, PickaxeManager.getInstance().getDefaultPickaxe());
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` WHERE `uuid`='" + player.getUniqueId().toString() + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                BigInteger availableBlocks = result.getBigDecimal(2).toBigInteger();
                BigInteger brokenBlocks = result.getBigDecimal(3).toBigInteger();

                Material pickaxeMaterial = Material.valueOf(result.getString(4));
                String pickaxeName = result.getString(5);
                Map<Enchant, Integer> enchants = deserializeEnchants(result.getString(6));

                Pickaxe pickaxe = new Pickaxe(pickaxeMaterial, pickaxeName, enchants);

                return new PlayerData(uuid, availableBlocks, brokenBlocks, pickaxe);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return null;
    }

    public List<PlayerData> getTopBrokenBlocks() {
        List<PlayerData> top = new ArrayList<>(10);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` ORDER BY `broken_blocks` DESC LIMIT 10;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                BigInteger availableBlocks = result.getBigDecimal(2).toBigInteger();
                BigInteger brokenBlocks = result.getBigDecimal(3).toBigInteger();

                Material pickaxeMaterial = Material.valueOf(result.getString(4));
                String pickaxeName = result.getString(5);
                Map<Enchant, Integer> enchants = deserializeEnchants(result.getString(6));

                Pickaxe pickaxe = new Pickaxe(pickaxeMaterial, pickaxeName, enchants);

                top.add(new PlayerData(uuid, availableBlocks, brokenBlocks, pickaxe));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return top;
    }

    private String serializeEnchants(Map<Enchant, Integer> enchants) {
        StringBuilder builder = new StringBuilder(24);
        for (Map.Entry<Enchant, Integer> entry : enchants.entrySet()) {
            Enchant enchant = entry.getKey();
            Integer level = entry.getValue();

            builder.append(enchant.getName()).append(",")
                    .append(level.toString()).append("#");
        }

        return builder.toString();
    }

    private Map<Enchant, Integer> deserializeEnchants(String serialized) {
        String[] split = serialized.split("#");
        Map<Enchant, Integer> ret = new HashMap<>(split.length / 2);

        for (String str : split) {
            String[] strSplit = str.split(",");

            Enchant enchant = DataManager.getInstance().getCache().getEnchant(strSplit[0]);
            if (enchant == null) continue;

            Integer level = Integer.parseInt(strSplit[1]);
            if (level < 0) continue;

            ret.put(enchant, level);
        }

        return ret;
    }

    private Boolean contains(String value, String column) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT `" + column + "` FROM `" + DBConnection.TABLE + "` WHERE `" + column + "`='" + value + "';";
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();
            return result.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return false;
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, null, null, statement);
        }
    }

    private void closeConnections(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + DBConnection.TABLE + "` (`uuid` VARCHAR(255), `available_blocks` DECIMAL(40,0), `broken_blocks` DECIMAL(40,0), `pickaxe_material` VARCHAR(50), `pickaxe_name` VARCHAR(256), `pickaxe_enchants` LONGTEXT, PRIMARY KEY(`uuid`));";
        executeUpdate(query);
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }
}