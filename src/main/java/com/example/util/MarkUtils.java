package com.example.util;

import com.example.entity.dto.StudentCourse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MarkUtils {
    //分配shId作业给value最小的resultNum个同学
    //同班同学、将自己排除在外
    //若人数不足取全部人数
    public List<StudentCourse> findAndAddToUnmarkList(int shId, String sidToExclude, int resultNum, List<StudentCourse> scList) {
        // 计算每个 StudentCourse 的值，并按照升序排序
        List<StudentCourse> sortedList = scList.stream()
                .filter(sc -> !sc.getSid().equals(sidToExclude))  // 排除相同 sid 的 StudentCourse
                .map(sc -> {
                    int value = sc.getMarkCount() * 2 + sc.getUnmarkCount();
                    sc.setComputedValue(value);
                    return sc;
                })
                .sorted(Comparator.comparingInt(StudentCourse::getComputedValue))
                .collect(Collectors.toList());

        // 选择前三个 StudentCourse
        List<StudentCourse> selectedList = sortedList.subList(0, Math.min(resultNum, sortedList.size()));
        //System.out.println(sortedList.size());

        // 调用 addToUnmarkList 方法
        for (StudentCourse sc : selectedList) {
            addToUnmarkList(sc, shId);
        }

        // 返回选中的三个 StudentCourse 组成的列表
        return new ArrayList<>(selectedList);
    }

    //添加批改任务
    //没考虑多次提交的情况
    public void addToUnmarkList(StudentCourse studentCourse, int shId) {
        // 将 shId 添加到 unmarkList 中
        appendShIdToUnmarkList(studentCourse, shId);

        // unmarkCount 加一
        studentCourse.setUnmarkCount(studentCourse.getUnmarkCount() + 1);
    }

    //完成批改任务，应检验unmarkList是否包含该任务
    //若不包含返回false
    public boolean moveShIdToMarkList(StudentCourse studentCourse, int shId) {
        // 获取 unmarkList
        String unmarkList = studentCourse.getUnmarkList();

        // 如果 unmarkList 包含给定的 shId
        if (unmarkList != null && unmarkList.contains(String.valueOf(shId))) {
            // 从 unmarkList 中移除 shId
            removeShIdFromUnmarkList(studentCourse, shId);

            // 将 shId 添加到 markList 中
            appendShIdToMarkList(studentCourse, shId);

            // unmarkCount 减一
            studentCourse.setUnmarkCount(studentCourse.getUnmarkCount() - 1);

            // markCount 加一
            studentCourse.setMarkCount(studentCourse.getMarkCount() + 1);
            return true;
        } else return false;
    }


    //增加shId到markList
    public void appendShIdToMarkList(StudentCourse studentCourse, int shId) {
        // 将 shId 转换为 String
        String shIdString = String.valueOf(shId);

        // 获取 markList
        String markList = studentCourse.getMarkList();

        // 如果 markList 为空，创建新的空字符串
        if (markList == null) {
            markList = "";
        }

        // 将 shIdString 添加到 markList 中并以逗号隔开
        if (!markList.isEmpty()) {
            markList += ",";
        }
        markList += shIdString;

        // 更新 StudentCourse 对象的 markList
        studentCourse.setMarkList(markList);
    }
    //添加shId到unmarkList
    public void appendShIdToUnmarkList(StudentCourse studentCourse, int shId) {
        // 将 shId 转换为 String
        String shIdString = String.valueOf(shId);

        // 获取 unmarkList
        String unmarkList = studentCourse.getUnmarkList();

        // 如果 unmarkList 为空，创建新的空字符串
        if (unmarkList == null) {
            unmarkList = "";
        }

        // 将 shIdString 添加到 unmarkList 中并以逗号隔开
        if (!unmarkList.isEmpty()) {
            unmarkList += ",";
        }
        unmarkList += shIdString;

        // 更新 StudentCourse 对象的 unmarkList
        studentCourse.setUnmarkList(unmarkList);
    }


    public boolean removeShIdFromUnmarkList(StudentCourse studentCourse, int shId) {
        // 获取 unmarkList
        String unmarkList = studentCourse.getUnmarkList();

        // 如果 unmarkList 为空，直接返回 false
        if (unmarkList == null || unmarkList.isEmpty()) {
            return false;
        }

        // 将 unmarkList 转换为数组
        String[] unmarkArray = unmarkList.split(",");

        // 将 String 数组转换为 List，方便操作
        List<String> unmarkListItems = new ArrayList<>(Arrays.asList(unmarkArray));

        // 尝试移除 shId
        if (unmarkListItems.remove(String.valueOf(shId))) {
            // 如果成功移除，更新 StudentCourse 对象的 unmarkList
            studentCourse.setUnmarkList(String.join(",", unmarkListItems));
            return true;
        }

        // 如果 unmarkList 不包含给定的 shId，则返回 false
        return false;
    }

    public int[] findShIdsInUnmarkList(List<StudentCourse> studentCourses) {
        // 使用 flatMap 操作，将每个 StudentCourse 的 unmarkList 转换为 int 数组
        int[] resultArray = studentCourses.stream()
                .map(StudentCourse::getUnmarkList)
                .filter(unmarkList -> unmarkList != null && !unmarkList.isEmpty())
                .flatMap(unmarkList -> Arrays.stream(unmarkList.split(","))
                        .mapToInt(Integer::parseInt)
                        .boxed())  // 将 IntStream 转换为 Stream<Integer>
                .mapToInt(Integer::intValue)  // 将 Stream<Integer> 转换为 IntStream
                .toArray();

        return resultArray;
    }

    //获取int[]类型unmarklist
    public int[] findShIdsInUnmarkList(StudentCourse studentCourse) {
        // 获取 unmarkList
        String unmarkList = studentCourse.getUnmarkList();

        // 如果 unmarkList 为空，返回空数组
        if (unmarkList == null || unmarkList.isEmpty()) {
            return null;
        }

        // 将 unmarkList 转换为数组
        String[] unmarkArray = unmarkList.split(",");

        // 将 String 数组转换为 int 数组
        int[] resultArray = Arrays.stream(unmarkArray)
                .mapToInt(Integer::parseInt)
                .toArray();

        return resultArray;
    }

    //获取int[]类型marklist
    public int[] findShIdsInMarkList(StudentCourse studentCourse) {
        // 获取 markList
        String markList = studentCourse.getMarkList();

        // 如果 markList 为空，返回空数组
        if (markList == null || markList.isEmpty()) {
            return null;
        }

        // 将 markList 转换为数组
        String[] markArray = markList.split(",");

        // 将 String 数组转换为 int 数组
        int[] resultArray = Arrays.stream(markArray)
                .mapToInt(Integer::parseInt)
                .toArray();

        return resultArray;
    }
}
