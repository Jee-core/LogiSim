package org.yourcompany.yourproject.Backend.dataAccessLayer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;

import org.yourcompany.yourproject.Backend.dataAccessLayer.util.DatabaseUtil;

public class DataBaseHandler implements IDAO {

    // =============================
    //          SAVE ROUTER
    // =============================
    // Returns generated ID, or -1 on failure
    public int save(Hashtable<String, String> data) {
        String table = data.get("table");
        if (table == null) return -1;

        return switch (table) {
            case "projects" -> saveProject(data);
            case "circuits" -> saveCircuit(data);
            case "components" -> saveComponent(data);
            case "connectors" -> saveConnector(data);
            case "component_values" -> saveComponentValue(data);
            default -> -1;
        };
    }

    // =============================
    //          DELETE
    // =============================
    @Override
    public boolean delete(String id) {
        String[] parts = id.split(":");
        if (parts.length != 2) return false;

        String table = parts[0];
        int pkId = Integer.parseInt(parts[1]);

        String pkColumn = switch (table) {
            case "projects" -> "project_id";
            case "circuits" -> "circuit_id";
            case "components" -> "component_id";
            case "connectors" -> "connector_id";
            case "component_values" -> "value_id";
            default -> null;
        };

        if (pkColumn == null) return false;

        String sql = "DELETE FROM " + table + " WHERE " + pkColumn + " = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =============================
    //          LOAD BY ID
    // =============================
    @Override
    public Hashtable<String, String> load(String id) {
        String[] parts = id.split(":");
        if (parts.length != 2) return null;

        String table = parts[0];
        int pkId = Integer.parseInt(parts[1]);

        String pkColumn = switch (table) {
            case "projects" -> "project_id";
            case "circuits" -> "circuit_id";
            case "components" -> "component_id";
            case "connectors" -> "connector_id";
            case "component_values" -> "value_id";
            default -> null;
        };

        if (pkColumn == null) return null;

        String sql = "SELECT * FROM " + table + " WHERE " + pkColumn + " = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pkId);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            Hashtable<String, String> record = new Hashtable<>();
            if (rs.next()) {
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    record.put(meta.getColumnName(i), rs.getString(i));
                }
            }
            return record;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ArrayList<Hashtable<String, String>> load() {
        return loadAll("projects");
    }

    // =============================
    //          LOAD ALL
    // =============================
    public ArrayList<Hashtable<String, String>> loadAll(String table) {
        ArrayList<Hashtable<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM " + table;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Hashtable<String, String> row = new Hashtable<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnName(i), rs.getString(i));
                }
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =============================
    //         SAVE PROJECT
    // =============================
    public int saveProject(Hashtable<String, String> data) {
        String sql = "INSERT INTO projects (name) VALUES (?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, data.get("name"));
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //         SAVE CIRCUIT
    // =============================
    public int saveCircuit(Hashtable<String, String> data) {
        String sql = "INSERT INTO circuits (project_id, name) VALUES (?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, Integer.parseInt(data.get("project_id")));
            stmt.setString(2, data.get("name"));
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //         SAVE COMPONENT
    // =============================
    public int saveComponent(Hashtable<String, String> data) {
        String sql = """
            INSERT INTO components 
            (circuit_id, name, type, inputs, outputs, position_x, position_y, id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, Integer.parseInt(data.get("circuit_id")));
            stmt.setString(2, data.get("name"));
            stmt.setString(3, data.get("type"));
            stmt.setInt(4, Integer.parseInt(data.get("inputs")));
            stmt.setInt(5, Integer.parseInt(data.get("outputs")));
            stmt.setInt(6, Integer.parseInt(data.get("position_x")));
            stmt.setInt(7, Integer.parseInt(data.get("position_y")));
            stmt.setString(8, data.get("id"));

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //         SAVE CONNECTOR
    // =============================
    public int saveConnector(Hashtable<String, String> data) {
        String sql = """
            INSERT INTO connectors 
            (circuit_id, name, color, from_component_id, from_port, to_component_id, to_port, signal_value, id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, Integer.parseInt(data.get("circuit_id")));
            stmt.setString(2, data.get("name"));
            stmt.setString(3, data.getOrDefault("color", "BLACK"));
            
            // Handle from_component_id (can be null)
            if (data.get("from_component_id") != null && !data.get("from_component_id").isEmpty()) {
                stmt.setInt(4, Integer.parseInt(data.get("from_component_id")));
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            stmt.setInt(5, Integer.parseInt(data.get("from_port")));
            
            // Handle to_component_id (can be null)
            if (data.get("to_component_id") != null && !data.get("to_component_id").isEmpty()) {
                stmt.setInt(6, Integer.parseInt(data.get("to_component_id")));
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setInt(7, Integer.parseInt(data.get("to_port")));
            stmt.setBoolean(8, Boolean.parseBoolean(data.getOrDefault("signal_value", "false")));
            stmt.setString(9, data.get("id"));

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //         SAVE COMPONENT VALUE
    // =============================
    public int saveComponentValue(Hashtable<String, String> data) {
        String sql = """
            INSERT INTO component_values 
            (component_id, port_index, value, type)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, Integer.parseInt(data.get("component_id")));
            stmt.setInt(2, Integer.parseInt(data.get("port_index")));
            stmt.setBoolean(3, Boolean.parseBoolean(data.get("value")));
            stmt.setString(4, data.get("type"));

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //      GENERIC FOREIGN KEY LOADER
    // =============================
    public ArrayList<Hashtable<String, String>> loadByForeignKey(String table, String column, int value) {
        ArrayList<Hashtable<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM " + table + " WHERE " + column + " = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            while (rs.next()) {
                Hashtable<String, String> row = new Hashtable<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnName(i), rs.getString(i));
                }
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =============================
    //      SPECIFIC LOAD METHODS
    // =============================
    
    public ArrayList<Hashtable<String, String>> loadCircuitsByProject(int projectId) {
        return loadByForeignKey("circuits", "project_id", projectId);
    }

    public ArrayList<Hashtable<String, String>> loadComponentsByCircuit(int circuitId) {
        return loadByForeignKey("components", "circuit_id", circuitId);
    }

    public ArrayList<Hashtable<String, String>> loadConnectorsByCircuit(int circuitId) {
        return loadByForeignKey("connectors", "circuit_id", circuitId);
    }

    public ArrayList<Hashtable<String, String>> loadComponentValuesByComponent(int componentId) {
        return loadByForeignKey("component_values", "component_id", componentId);
    }

    // =============================
    //      DELETE BY FOREIGN KEY
    // =============================
    public boolean deleteByForeignKey(String table, String column, int value) {
        String sql = "DELETE FROM " + table + " WHERE " + column + " = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =============================
    //      UPDATE METHODS
    // =============================
    
    public boolean updateComponentPosition(int componentId, int positionX, int positionY) {
        String sql = "UPDATE components SET position_x = ?, position_y = ? WHERE component_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, positionX);
            stmt.setInt(2, positionY);
            stmt.setInt(3, componentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateConnectorSignal(int connectorId, boolean signalValue) {
        String sql = "UPDATE connectors SET signal_value = ? WHERE connector_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, signalValue);
            stmt.setInt(2, connectorId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateComponentValue(int componentId, int portIndex, boolean value, String type) {
        // First try to update, if no rows affected then insert
        String updateSql = """
            UPDATE component_values 
            SET value = ? 
            WHERE component_id = ? AND port_index = ? AND type = ?
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            stmt.setBoolean(1, value);
            stmt.setInt(2, componentId);
            stmt.setInt(3, portIndex);
            stmt.setString(4, type);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                return true;
            }

            // If no update happened, insert new record
            Hashtable<String, String> data = new Hashtable<>();
            data.put("table", "component_values");
            data.put("component_id", String.valueOf(componentId));
            data.put("port_index", String.valueOf(portIndex));
            data.put("value", String.valueOf(value));
            data.put("type", type);
            
            return saveComponentValue(data) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}