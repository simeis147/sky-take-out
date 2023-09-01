package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 插入套餐菜品表
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 通过套餐查询是否关联菜品
     * @param setmealIds
     * @return
     */
    List<Long> getDishIdsBySetmealIds(List<Long> setmealIds);

    /**
     * 通过套餐id删除套餐菜品
     * @param setmelId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmelId}")
    void deleteBySetmealId(Long setmelId);

    @Select("select * from setmeal_dish where setmeal_id = #{stmealId}")
    @AutoFill(OperationType.UPDATE)
    List<SetmealDish> getBySetmealId(Long id);
}
