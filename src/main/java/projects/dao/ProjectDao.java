package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	public Project insertProject(Project project) {
	// @formatter:off
		String sql = ""
				+ "INSERT INTO " + PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?)";
		// @formatter:on
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class); // Fixed the issue with a mentor
				setParameter(stmt, 5, project.getNotes(), String.class);

				stmt.executeUpdate();
				Integer projectid = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);

				project.setProjectId(projectid);
				return project;

			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);

		}
	}

	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				try (ResultSet rs = stmt.executeQuery()) {
					List<Project> projects = new LinkedList<>();

					while (rs.next()) {

						projects.add(extract(rs, Project.class));
						// ^^ Fixed the Issue

						// --This works fine --
						// Project Project = new Project();

						// Project.setActualHours(rs.getBigDecimal("actual_hours"));
						// Project.setDifficulty(rs.getObject("difficulty", Integer.class));
						// Project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
						// Project.setNotes(rs.getString("notes"));
						// Project.setProjectId(rs.getObject("project_id", Integer.class));
						// Project.setProjectName(rs.getString("project_name"));

						// projects.add(Project);
						// ---------------------
					}
					return projects;
				}
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}

	}

	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try {
				Project project = null;

				try (PreparedStatement stmt = conn.prepareStatement(sql)) {
					setParameter(stmt, 1, projectId, Integer.class);

					try (ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							project = extract(rs, Project.class);
						}
					}

				}

				if (Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchProjectMaterials(conn, projectId));
					project.getSteps().addAll(fetchProjectSteps(conn, projectId));
					project.getCategories().addAll(fetchProjectCategories(conn, projectId));
				}
				commitTransaction(conn);

				return Optional.ofNullable(project);
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}

		} catch (SQLException e) {
			throw new DbException(e);
		}

	}

	private List<Category> fetchProjectCategories(Connection conn, Integer projectId) throws SQLException {
		//@formatter:off
		String sql = ""
		+ "SELECT c.* FROM " + CATEGORY_TABLE + " c " 
		+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "	
		+ "WHERE project_id = ?";
		//@formatter:on
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Category> categories = new LinkedList<>(); // testing this

				while (rs.next()) {
					// this should work when fetchAllProjects is running correctly,
					categories.add(extract(rs, Category.class));
				}
				return categories;
			}
		}
	}

	private List<Step> fetchProjectSteps(Connection conn, Integer projectId) throws SQLException {

		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<>(); // testing this

				while (rs.next()) {
					// this should work when fetchAllProjects is running correctly,

					steps.add(extract(rs, Step.class));

				}
				return steps;
			}
		}
	}

	private List<Material> fetchProjectMaterials(Connection conn, Integer projectId) throws SQLException {

		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Material> materials = new LinkedList<>(); // testing this

				while (rs.next()) {
					// this should work when fetchAllProjects is running correctly,

					materials.add(extract(rs, Material.class));

				}
				return materials;
			}
		}
	}

	public boolean modifyProjectDetails(Project project) {
	// @formatter:off
		String sql = "" + "UPDATE " + PROJECT_TABLE + " SET "
				   + "project_name = ?, " + "estimated_hours = ?, "
				   + "actual_hours = ?, " + "difficulty = ?, "
				   + "notes = ? " + "WHERE project_id = ?";
	// @formatter:on
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class); // Fixed the issue with a mentor
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);

				boolean result = stmt.executeUpdate() == 1;

				commitTransaction(conn);

				return result;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}

		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	public boolean deleteProject(Integer projectId) {
		// formatter:off
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		// formatter:on
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);

				boolean result = stmt.executeUpdate() == 1;

				commitTransaction(conn);

				return result;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}

		} catch (SQLException e) {
			throw new DbException(e);
		}

	}
}
