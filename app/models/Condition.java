package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Reference;
import es.common.ElasticClient;
import models.es.ProjectDoc;
import org.apache.commons.lang.StringUtils;
import play.modules.morphia.Model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guoqingma on 16-11-25.
 * 状态码
 */
@Entity
public class Condition extends Model {

    public String code;//状态码
    public String describe;//描述
    public Date createDate;
    @Reference
    public Project project;


    public Condition(String code, String describe, String projectId) {
        this.describe = describe;
        this.code = code;
        this.createDate = new Date();
        this.project = Project.findById(projectId);
    }

    public static void saveCondition(String id, String code, String describe, String projectId) {
        if (StringUtils.isNotEmpty(id)) {
            Condition condition = Condition.findById(id);
            condition.code = code;
            condition.describe = describe;
            condition.save();
            ElasticClient.updateDocuments(id, "", "index", "condition");
        } else {
            Condition condition = new Condition(code, describe, projectId);
            condition.save();
            ProjectDoc projectDoc = new ProjectDoc(String.valueOf(condition.getId()), condition.project.name, condition.code, condition.describe);
            ElasticClient.createDocuments(projectDoc, "index", "condition");
        }
    }

    public static List<Condition> getConditionListByProject(String projectId) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("projectId", projectId);
        return queryByCondition(map).asList();
    }

    private static MorphiaQuery queryByCondition(Map<String, String> condition) {
        MorphiaQuery mQuery = Condition.q();
        if (condition != null) {
            if (StringUtils.isNotEmpty(condition.get("projectId"))) {
                mQuery = mQuery.filter("project", Project.findById(condition.get("projectId")));
            }
            if (StringUtils.isNotEmpty(condition.get("id"))) {
                mQuery = mQuery.filter("id", condition.get("id"));
            }
        }
        return mQuery;
    }

    public static void deleteById(String id) {
        Condition.findById(id).delete();
        ElasticClient.deleltDocuments(id, "index", "condition");
    }
}
