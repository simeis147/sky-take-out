package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.insertWithDish(setmeal);

        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0){
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealId);
            }
            // 向套餐菜品表插入n条数据
            setmealDishMapper.insertBatch(setmealDishes);
        }


    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    public void deleteWithSetmealDish(List<Long> ids) {
        Setmeal setmeal = new Setmeal();

        // 是否存在起售中的套餐
        if (ids != null && ids.size() > 0) {
            for (Long id : ids) {
                setmeal = setmealMapper.getById(id);
                if(setmeal.getStatus() == StatusConstant.ENABLE){
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
                }
            }
        }

        // 是否被菜品关联了
/*        List<Long> DishIds = setmealDishMapper.getDishIdsBySetmealIds(ids);
        if ( DishIds != null && DishIds.size() > 0 ){
            //当前套餐里面含有菜品， 不能删除
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }*/

        //删除菜品表中的菜品数据
        for (Long id : ids) {
            setmealMapper.deleteById(id);
            setmealDishMapper.deleteBySetmealId(id);
        }

    }

    /**
     * 根据ID查询套餐
     * @param id
     * @return
     */
    public SetmealVO getById(Long id) {
        // 根据ID查询套餐数据
        Setmeal setmeal = setmealMapper.getById(id);

        // 根据ID查询套餐菜品数据
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        // 将查询到的数据封装到VO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    public void updateWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        // 修改套餐的基本信息
        setmealMapper.update(setmeal);

        // 删除套餐菜品数据
        setmealDishMapper.deleteBySetmealId(setmeal.getId());

        // 重新插入套餐菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        if(setmealDishes != null && setmealDishes.size() > 0){
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealDTO.getId());
            }

            // 向套餐菜品表插入数据
            setmealDishMapper.insertBatch(setmealDishes);

        }



    }

    /**
     * 套餐起售、停售
     * @param status
     */
    public void stopOrStartSetmeal(Integer status, Long id) {

        Setmeal setmealMapperById = setmealMapper.getById(id);

        // 套餐内包含未启售菜品，无法启售
        if ( setmealMapperById.getStatus() == StatusConstant.DISABLE ){
            List<Integer> getStatus = setmealMapper.selectDishStatus(id);
            if (getStatus != null && getStatus.size() > 0){
                for (Integer s : getStatus) {
                    if (s == StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .status(status)
                .id(id)
                .build();

        setmealMapper.update(setmeal);
    }
}
