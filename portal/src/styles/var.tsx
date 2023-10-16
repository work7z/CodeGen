import { TreeNodeInfo } from "@blueprintjs/core";
import { AnyAction, createSlice, PayloadAction } from "@reduxjs/toolkit";
export const CLZ_ROOT_DARK = `bp5-dark`;
export const CLZ_ROOT_LIGHT = `bp5-light bp5-bg-light`;
export const CLZ_SECOND_TEXT = "bp5-text-muted";
export const CLZ_SMALL_TEXT = "bp5-text-small";

export type IsOKType = PayloadAction<{ isOK: boolean }>;
export type IsLoadingType = PayloadAction<{ isLoading: boolean }>;
export type SendErrorAction = PayloadAction<{ e: Error }>;
export type TextValueAction = PayloadAction<{ value: string }>;
export type LangDefinition = { [key: string]: string };

export type PromiseAction = PayloadAction<{
  id: string;
  fn: () => Promise<any>;
}>;

export type PayloadListData<T> = { payload: { list: T[] } };
export type PayloadValueData<T> = { payload: { value: T } };

export type TreeWrapInfo = {
  updateId: string;
  nodes: TreeNodeInfo[];
  selected?: string[];
  expanded?: string[];
};

export type LocalResponse = {};

export const LANG_ZH_CN = "zh_CN";
export const LANG_ZH_HK = "zh_HK";
export const LANG_EN_US = "en_US";

export interface ToolParamType {
  extId: string | null;
  category: string | null;
}
export type AnyMapType = { [key: string]: any };
