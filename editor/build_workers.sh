ROOT=../node_modules/monaco-editor/esm/vs
OPTS="--no-source-maps --log-level verbose"        # Parcel options - See: https://parceljs.org/cli.html

echo $ROOT $OPTS

parcel build $ROOT/language/json/json.worker.js $OPTS
parcel build $ROOT/language/css/css.worker.js $OPTS
parcel build $ROOT/language/html/html.worker.js $OPTS
parcel build $ROOT/language/typescript/ts.worker.js $OPTS
parcel build $ROOT/editor/editor.worker.js $OPTS